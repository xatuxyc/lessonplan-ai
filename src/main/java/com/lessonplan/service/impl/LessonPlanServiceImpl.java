package com.lessonplan.service.impl;

import com.alibaba.fastjson.JSON;
import com.lessonplan.ai.LessonPlanAiService;
import com.lessonplan.entity.config.AppConfig;
import com.lessonplan.entity.dto.LessonPlanParseDto;
import com.lessonplan.entity.enums.FileTypeEnum;
import com.lessonplan.entity.enums.LessonPlanStatusEnum;
import com.lessonplan.entity.enums.PageSize;
import com.lessonplan.entity.enums.ResponseCodeEnum;
import com.lessonplan.entity.po.LessonPlan;
import com.lessonplan.entity.po.LessonPlanParseResult;
import com.lessonplan.entity.query.LessonPlanQuery;
import com.lessonplan.entity.query.SimplePage;
import com.lessonplan.entity.vo.PaginationResultVO;
import com.lessonplan.exception.BusinessException;
import com.lessonplan.mappers.LessonPlanMapper;
import com.lessonplan.mappers.LessonPlanParseResultMapper;
import com.lessonplan.service.LessonPlanParseResultService;
import com.lessonplan.service.LessonPlanService;
import jakarta.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * 教案文件Service实现
 */
@Service("lessonPlanService")
public class LessonPlanServiceImpl implements LessonPlanService {

    private static final Logger logger = LoggerFactory.getLogger(LessonPlanServiceImpl.class);

    @Resource
    private LessonPlanMapper lessonPlanMapper;

    @Resource
    private LessonPlanParseResultMapper lessonPlanParseResultMapper;

    @Resource
    private LessonPlanParseResultService lessonPlanParseResultService;

    @Resource
    private AppConfig appConfig;

    @Resource(name = "mockAiService")
    private LessonPlanAiService mockAiService;

    @Resource(name = "ollamaAiService")
    private LessonPlanAiService ollamaAiService;

    @Override
    public List<LessonPlan> findListByParam(LessonPlanQuery param) {
        return lessonPlanMapper.selectList(param);
    }

    @Override
    public Integer findCountByParam(LessonPlanQuery param) {
        return lessonPlanMapper.selectCount(param);
    }

    @Override
    public PaginationResultVO<LessonPlan> findListByPage(LessonPlanQuery param) {
        int count = this.findCountByParam(param);
        int pageSize = param.getPageSize() == null ? PageSize.SIZE15.getSize() : param.getPageSize();
        SimplePage page = new SimplePage(param.getPageNo(), count, pageSize);
        param.setSimplePage(page);
        List<LessonPlan> list = this.findListByParam(param);
        return new PaginationResultVO<>(count, page.getPageSize(), page.getPageNo(), page.getPageTotal(), list);
    }

    @Override
    public LessonPlan getLessonPlanById(Integer id) {
        return lessonPlanMapper.selectById(id);
    }

    @Override
    public Integer add(LessonPlan bean) {
        return lessonPlanMapper.insert(bean);
    }

    @Override
    public Integer updateLessonPlanById(LessonPlan bean, Integer id) {
        return lessonPlanMapper.updateById(bean, id);
    }

    @Override
    public Integer deleteLessonPlanById(Integer id) {
        // 删除文件
        LessonPlan lessonPlan = this.getLessonPlanById(id);
        if (lessonPlan != null && StringUtils.isNotEmpty(lessonPlan.getFilePath())) {
            try {
                Files.deleteIfExists(Paths.get(lessonPlan.getFilePath()));
            } catch (IOException e) {
                logger.error("删除文件失败: {}", lessonPlan.getFilePath(), e);
            }
        }
        // 删除解析结果
        lessonPlanParseResultMapper.deleteByLessonPlanId(id);
        // 删除记录
        return lessonPlanMapper.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public LessonPlan uploadFile(String originalFilename, String fileType, Long fileSize,
                                 String uploader, File tempFile) {
        // 1. 校验文件类型
        if (!FileTypeEnum.isValid(fileType)) {
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        }

        // 2. 校验文件大小
        if (fileSize > appConfig.getMaxFileSize()) {
            throw new BusinessException(ResponseCodeEnum.CODE_602);
        }

        // 3. 生成存储文件名和路径
        String storedName = UUID.randomUUID().toString().replace("-", "") + "." + fileType;
        String folder = appConfig.getProjectFolder() + "lessonplan/";
        Path folderPath = Paths.get(folder);
        if (!Files.exists(folderPath)) {
            try {
                Files.createDirectories(folderPath);
            } catch (IOException e) {
                throw new BusinessException("创建存储目录失败", e);
            }
        }

        String filePath = folder + storedName;
        try {
            Files.copy(tempFile.toPath(), Paths.get(filePath));
        } catch (IOException e) {
            throw new BusinessException("保存文件失败", e);
        }

        // 4. 插入数据库记录
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

        // 1. 更新状态为"解析中"
        LessonPlan updateBean = new LessonPlan();
        updateBean.setStatus(LessonPlanStatusEnum.PARSING.getStatus());
        lessonPlanMapper.updateById(updateBean, lessonPlanId);

        long startTime = System.currentTimeMillis();
        try {
            // 2. 提取文件文本内容
            String content = extractText(lessonPlan);
            if (StringUtils.isEmpty(content)) {
                throw new BusinessException("无法提取文件内容");
            }

            // 3. 调用AI解析
            LessonPlanAiService aiService = getAiService();
            LessonPlanParseDto parseDto = aiService.parse(content);
            long duration = System.currentTimeMillis() - startTime;

            // 4. 保存解析结果
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

            // 5. 更新状态为"解析成功"
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
            failResult.setModelName(getAiService().getModelName());
            failResult.setIsMock(getAiService().isMock() ? 1 : 0);
            failResult.setFailReason(e.getMessage());
            failResult.setParseTime(new Date());
            lessonPlanParseResultMapper.insert(failResult);

            // 更新状态为"解析失败"
            LessonPlan failBean = new LessonPlan();
            failBean.setStatus(LessonPlanStatusEnum.PARSE_FAILED.getStatus());
            lessonPlanMapper.updateById(failBean, lessonPlanId);
        }
    }

    /**
     * 提取文件文本内容
     */
    private String extractText(LessonPlan lessonPlan) {
        String filePath = lessonPlan.getFilePath();
        String fileType = lessonPlan.getFileType();

        try {
            switch (fileType.toLowerCase()) {
                case "pdf":
                    return extractPdfText(filePath);
                case "doc":
                case "docx":
                    return extractDocText(filePath);
                default:
                    throw new BusinessException("不支持的文件类型: " + fileType);
            }
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("文件内容提取失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用PDFBox提取PDF文本
     */
    private String extractPdfText(String filePath) throws Exception {
        try (org.apache.pdfbox.pdmodel.PDDocument document = org.apache.pdfbox.Loader.loadPDF(new File(filePath))) {
            org.apache.pdfbox.text.PDFTextStripper stripper = new org.apache.pdfbox.text.PDFTextStripper();
            return stripper.getText(document);
        }
    }

    /**
     * 使用POI提取DOC/DOCX文本
     */
    private String extractDocText(String filePath) throws Exception {
        StringBuilder text = new StringBuilder();
        if (filePath.endsWith(".docx")) {
            try (org.apache.poi.xwpf.usermodel.XWPFDocument doc = new org.apache.poi.xwpf.usermodel.XWPFDocument(
                    Files.newInputStream(Paths.get(filePath)))) {
                for (org.apache.poi.xwpf.usermodel.XWPFParagraph para : doc.getParagraphs()) {
                    text.append(para.getText()).append("\n");
                }
            }
        } else {
            // .doc 格式使用 HWPFDocument
            try (org.apache.poi.hwpf.HWPFDocument doc = new org.apache.poi.hwpf.HWPFDocument(
                    Files.newInputStream(Paths.get(filePath)))) {
                text.append(doc.getText());
            }
        }
        return text.toString();
    }

    /**
     * 根据配置获取AI服务实现
     */
    private LessonPlanAiService getAiService() {
        if (!appConfig.getAiServiceEnabled()) {
            return mockAiService;
        }
        String mode = appConfig.getAiServiceMode();
        if ("ollama".equalsIgnoreCase(mode)) {
            return ollamaAiService;
        }
        return mockAiService;
    }
}
