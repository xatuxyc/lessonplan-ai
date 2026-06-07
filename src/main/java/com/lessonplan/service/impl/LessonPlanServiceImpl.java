package com.lessonplan.service.impl;

import com.alibaba.fastjson.JSON;
import com.lessonplan.ai.LessonPlanAiService;
import com.lessonplan.entity.dto.LessonPlanParseDto;
import com.lessonplan.entity.enums.FileTypeEnum;
import com.lessonplan.entity.enums.LessonPlanStatusEnum;
import com.lessonplan.entity.po.LessonPlan;
import com.lessonplan.entity.po.LessonPlanParseResult;
import com.lessonplan.entity.query.LessonPlanQuery;
import com.lessonplan.entity.vo.PaginationResultVO;
import com.lessonplan.mappers.LessonPlanMapper;
import com.lessonplan.mappers.LessonPlanParseResultMapper;
import com.lessonplan.service.LessonPlanService;
import jakarta.annotation.Resource;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.hwpf.HWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service("lessonPlanService")
public class LessonPlanServiceImpl implements LessonPlanService {

    private static final Logger logger = LoggerFactory.getLogger(LessonPlanServiceImpl.class);
    private static final long MAX_FILE_SIZE = 50L * 1024 * 1024; // 50MB

    @Value("${project.folder:e:/code/files/lessonplan/}")
    private String storageBasePath;

    @Resource
    private LessonPlanMapper lessonPlanMapper;
    @Resource
    private LessonPlanParseResultMapper lessonPlanParseResultMapper;
    @Resource
    private LessonPlanAiService aiService;

    @Override
    public PaginationResultVO<LessonPlan> findListByPage(LessonPlanQuery param) {
        Integer pageNo = param.getPageNo();
        Integer pageSize = param.getPageSize();
        int offset = (pageNo - 1) * pageSize;
        int count = lessonPlanMapper.selectCount();
        List<LessonPlan> list = lessonPlanMapper.selectList(offset, pageSize);
        int pageTotal = (count + pageSize - 1) / pageSize;
        return new PaginationResultVO<>(count, pageSize, pageNo, pageTotal, list);
    }

    @Override
    public List<LessonPlan> findListByParam(LessonPlanQuery param) {
        return lessonPlanMapper.selectList(0, 100);
    }

    @Override
    public Integer findCountByParam(LessonPlanQuery param) {
        return lessonPlanMapper.selectCount();
    }

    @Override
    public Integer add(LessonPlan bean) {
        return lessonPlanMapper.insert(bean);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer updateLessonPlanById(LessonPlan bean, Integer id) {
        return lessonPlanMapper.updateById(bean, id);
    }

    @Override
    public LessonPlan getLessonPlanById(Integer id) {
        return lessonPlanMapper.selectById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LessonPlan uploadFile(String originalFilename, String fileType, Long fileSize,
                                 String uploader, File tempFile) {
        if (!FileTypeEnum.isValid(fileType)) {
            throw new RuntimeException("不支持的文件类型，仅支持 PDF/DOC/DOCX");
        }
        if (fileSize > MAX_FILE_SIZE) {
            throw new RuntimeException("文件大小超出50MB限制");
        }

        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + fileType;
        String folder = storageBasePath + "lessonplan/";
        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = folder + storedName;
        try {
            Files.copy(tempFile.toPath(), Paths.get(filePath));
        } catch (IOException e) {
            throw new RuntimeException("保存文件失败", e);
        }

        LessonPlan bean = new LessonPlan();
        bean.setFileName(originalFilename);
        bean.setStoredName(storedName);
        bean.setFileType(fileType);
        bean.setFileSize(fileSize);
        bean.setUploader(uploader);
        bean.setUploadTime(new Date());
        bean.setStatus(LessonPlanStatusEnum.UPLOADED.getStatus());
        bean.setFilePath(filePath);
        lessonPlanMapper.insert(bean);
        return bean;
    }

    @Override
    @Async
    public void triggerParse(Integer lessonPlanId) {
        LessonPlan lessonPlan = this.getLessonPlanById(lessonPlanId);
        if (lessonPlan == null) {
            logger.error("教案不存在: id={}", lessonPlanId);
            return;
        }

        // 更新状态为"解析中"
        LessonPlan updateBean = new LessonPlan();
        updateBean.setStatus(LessonPlanStatusEnum.PARSING.getStatus());
        lessonPlanMapper.updateById(updateBean, lessonPlanId);

        long startTime = System.currentTimeMillis();
        try {
            // 提取文件文本内容
            String content = extractText(lessonPlan);
            if (content == null || content.trim().isEmpty()) {
                throw new RuntimeException("无法提取文件内容");
            }

            // 调用AI解析
            LessonPlanParseDto parseDto = aiService.parse(content);
            long duration = System.currentTimeMillis() - startTime;

            // 保存解析结果
            LessonPlanParseResult result = new LessonPlanParseResult();
            result.setLessonPlanId(lessonPlanId);
            result.setTitle(parseDto.getTitle());
            result.setGradeLevel(parseDto.getGradeLevel());
            result.setSubject(parseDto.getSubject());
            result.setTeachingObjectives(JSON.toJSONString(parseDto.getTeachingObjectives()));
            result.setKeywords(JSON.toJSONString(parseDto.getKeywords()));
            result.setSummary(parseDto.getSummary());
            result.setDuration(duration);
            result.setModelName(aiService.getModelName());
            result.setIsMock(aiService.isMock() ? 1 : 0);
            result.setParseTime(new Date());
            lessonPlanParseResultMapper.insert(result);

            // 更新状态为"解析成功"
            LessonPlan successBean = new LessonPlan();
            successBean.setStatus(LessonPlanStatusEnum.PARSE_SUCCESS.getStatus());
            lessonPlanMapper.updateById(successBean, lessonPlanId);

            logger.info("教案解析成功: id={}, 耗时={}ms, 模型={}", lessonPlanId, duration, aiService.getModelName());

        } catch (Exception e) {
            logger.error("教案解析失败: id={}", lessonPlanId, e);
            long duration = System.currentTimeMillis() - startTime;

            // 保存失败原因
            LessonPlanParseResult failResult = new LessonPlanParseResult();
            failResult.setLessonPlanId(lessonPlanId);
            failResult.setDuration(duration);
            failResult.setModelName(aiService.getModelName());
            failResult.setIsMock(aiService.isMock() ? 1 : 0);
            failResult.setFailReason(e.getMessage());
            failResult.setParseTime(new Date());
            lessonPlanParseResultMapper.insert(failResult);

            // 更新状态为"解析失败"
            LessonPlan failBean = new LessonPlan();
            failBean.setStatus(LessonPlanStatusEnum.PARSE_FAILED.getStatus());
            lessonPlanMapper.updateById(failBean, lessonPlanId);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Integer deleteLessonPlanById(Integer id) {
        LessonPlan lessonPlan = this.getLessonPlanById(id);
        if (lessonPlan != null && lessonPlan.getFilePath() != null) {
            try {
                Files.deleteIfExists(Paths.get(lessonPlan.getFilePath()));
            } catch (IOException e) {
                logger.error("删除文件失败: {}", lessonPlan.getFilePath(), e);
            }
        }
        lessonPlanParseResultMapper.deleteByLessonPlanId(id);
        lessonPlanMapper.deleteById(id);
        return 1;
    }

    private String extractText(LessonPlan lessonPlan) {
        String filePath = lessonPlan.getFilePath();
        String fileType = lessonPlan.getFileType();
        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    return extractPdfText(filePath);
                case "doc":
                    return extractDocText(filePath);
                case "docx":
                    return extractDocxText(filePath);
                default:
                    throw new RuntimeException("不支持的文件类型: " + fileType);
            }
        } catch (Exception e) {
            throw new RuntimeException("文件内容提取失败: " + e.getMessage(), e);
        }
    }

    private String extractPdfText(String filePath) throws IOException {
        try (PDDocument document = Loader.loadPDF(new File(filePath))) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    private String extractDocText(String filePath) throws IOException {
        try (InputStream fis = new FileInputStream(filePath);
             HWPFDocument document = new HWPFDocument(fis)) {
            return document.getText().toString();
        }
    }

    private String extractDocxText(String filePath) throws IOException {
        StringBuilder text = new StringBuilder();
        try (InputStream fis = new FileInputStream(filePath);
             XWPFDocument document = new XWPFDocument(fis)) {
            for (var para : document.getParagraphs()) {
                text.append(para.getText()).append("\n");
            }
        }
        return text.toString();
    }
}
