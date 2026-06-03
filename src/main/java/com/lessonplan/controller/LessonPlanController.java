package com.lessonplan.controller;

import com.lessonplan.entity.enums.ResponseCodeEnum;
import com.lessonplan.entity.po.LessonPlan;
import com.lessonplan.entity.po.LessonPlanParseResult;
import com.lessonplan.entity.query.LessonPlanQuery;
import com.lessonplan.entity.vo.PaginationResultVO;
import com.lessonplan.entity.vo.ResponseVO;
import com.lessonplan.exception.BusinessException;
import com.lessonplan.service.LessonPlanParseResultService;
import com.lessonplan.service.LessonPlanService;
import com.lessonplan.controller.base.BaseController;
import jakarta.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 教案文件Controller
 */
@RestController
@RequestMapping("/lessonplan")
public class LessonPlanController extends BaseController {

    private static final Logger logger = LoggerFactory.getLogger(LessonPlanController.class);

    @Resource
    private LessonPlanService lessonPlanService;

    @Resource
    private LessonPlanParseResultService lessonPlanParseResultService;

    /**
     * 上传教案文件
     * POST /api/lessonplan/upload
     */
    @PostMapping("/upload")
    public ResponseVO upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "uploader", defaultValue = "anonymous") String uploader) {
        if (file.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_600);
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = FilenameUtils.getExtension(originalFilename);
        if (suffix == null || suffix.isEmpty()) {
            throw new BusinessException(ResponseCodeEnum.CODE_603);
        }
        suffix = suffix.toLowerCase();

        Long fileSize = file.getSize();

        // 保存临时文件
        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "." + suffix);
            file.transferTo(tempFile);
        } catch (IOException e) {
            throw new BusinessException("文件保存失败", e);
        }

        try {
            LessonPlan lessonPlan = lessonPlanService.uploadFile(originalFilename, suffix, fileSize, uploader, tempFile);
            return getSuccessResponseVO(lessonPlan);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    /**
     * 查询教案文件列表（分页）
     * POST /api/lessonplan/list
     */
    @PostMapping("/list")
    public ResponseVO list(@RequestBody LessonPlanQuery query) {
        PaginationResultVO<LessonPlan> result = lessonPlanService.findListByPage(query);
        return getSuccessResponseVO(result);
    }

    /**
     * 查询教案文件详情
     * GET /api/lessonplan/detail/{id}
     */
    @GetMapping("/detail/{id}")
    public ResponseVO detail(@PathVariable("id") Integer id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "教案不存在");
        }

        // 同时查询解析结果
        LessonPlanParseResult parseResult = lessonPlanParseResultService.getByLessonPlanId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("lessonPlan", lessonPlan);
        data.put("parseResult", parseResult);
        return getSuccessResponseVO(data);
    }

    /**
     * 触发AI解析
     * POST /api/lessonplan/parse/{id}
     */
    @PostMapping("/parse/{id}")
    public ResponseVO parse(@PathVariable("id") Integer id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "教案不存在");
        }

        // 异步触发解析
        lessonPlanService.triggerParse(id);
        return getSuccessResponseVO("解析任务已提交");
    }

    /**
     * 查询解析状态
     * GET /api/lessonplan/parseStatus/{id}
     */
    @GetMapping("/parseStatus/{id}")
    public ResponseVO parseStatus(@PathVariable("id") Integer id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            throw new BusinessException(ResponseCodeEnum.CODE_600.getCode(), "教案不存在");
        }

        LessonPlanParseResult parseResult = lessonPlanParseResultService.getByLessonPlanId(id);

        Map<String, Object> data = new HashMap<>();
        data.put("id", lessonPlan.getId());
        data.put("status", lessonPlan.getStatus());
        data.put("parseResult", parseResult);
        return getSuccessResponseVO(data);
    }

    /**
     * 删除教案文件
     * DELETE /api/lessonplan/delete/{id}
     */
    @DeleteMapping("/delete/{id}")
    public ResponseVO delete(@PathVariable("id") Integer id) {
        lessonPlanService.deleteLessonPlanById(id);
        return getSuccessResponseVO(null);
    }
}
