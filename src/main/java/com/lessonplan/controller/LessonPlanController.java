package com.lessonplan.controller;

import com.lessonplan.entity.po.LessonPlan;
import com.lessonplan.entity.po.LessonPlanParseResult;
import com.lessonplan.entity.query.LessonPlanQuery;
import com.lessonplan.entity.vo.PaginationResultVO;
import com.lessonplan.entity.vo.ResponseVO;
import com.lessonplan.service.LessonPlanParseResultService;
import com.lessonplan.service.LessonPlanService;
import jakarta.annotation.Resource;
import org.apache.commons.io.FilenameUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/lessonplan")
public class LessonPlanController {

    @Resource
    private LessonPlanService lessonPlanService;
    @Resource
    private LessonPlanParseResultService lessonPlanParseResultService;

    @PostMapping("/upload")
    public ResponseVO upload(@RequestParam("file") MultipartFile file,
                             @RequestParam(value = "uploader", defaultValue = "anonymous") String uploader) {
        if (file.isEmpty()) {
            return new ResponseVO("error", 400, "文件不能为空", null);
        }

        String originalFilename = file.getOriginalFilename();
        String suffix = FilenameUtils.getExtension(originalFilename);
        if (suffix == null || suffix.isEmpty()) {
            return new ResponseVO("error", 400, "文件格式错误", null);
        }
        suffix = suffix.toLowerCase();

        Long fileSize = file.getSize();

        File tempFile = null;
        try {
            tempFile = File.createTempFile("upload_", "." + suffix);
            file.transferTo(tempFile);
            LessonPlan lessonPlan = lessonPlanService.uploadFile(originalFilename, suffix, fileSize, uploader, tempFile);
            return new ResponseVO("success", 200, null, lessonPlan);
        } catch (Exception e) {
            return new ResponseVO("error", 500, e.getMessage(), null);
        } finally {
            if (tempFile != null && tempFile.exists()) {
                tempFile.delete();
            }
        }
    }

    @PostMapping("/list")
    public ResponseVO list(@RequestBody Map<String, Integer> params) {
        LessonPlanQuery query = new LessonPlanQuery();
        query.setPageNo(params.getOrDefault("pageNo", 1));
        query.setPageSize(params.getOrDefault("pageSize", 20));
        PaginationResultVO<LessonPlan> result = lessonPlanService.findListByPage(query);
        return new ResponseVO("success", 200, null, result);
    }

    @GetMapping("/detail/{id}")
    public ResponseVO detail(@PathVariable("id") Integer id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            return new ResponseVO("error", 404, "教案不存在", null);
        }

        LessonPlanParseResult parseResult = lessonPlanParseResultService.getByLessonPlanId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("lessonPlan", lessonPlan);
        data.put("parseResult", parseResult);
        return new ResponseVO("success", 200, null, data);
    }

    @PostMapping("/parse/{id}")
    public ResponseVO parse(@PathVariable("id") Integer id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            return new ResponseVO("error", 404, "教案不存在", null);
        }

        lessonPlanService.triggerParse(id);
        return new ResponseVO("success", 200, "解析任务已提交", null);
    }

    @GetMapping("/parseStatus/{id}")
    public ResponseVO parseStatus(@PathVariable("id") Integer id) {
        LessonPlan lessonPlan = lessonPlanService.getLessonPlanById(id);
        if (lessonPlan == null) {
            return new ResponseVO("error", 404, "教案不存在", null);
        }

        LessonPlanParseResult parseResult = lessonPlanParseResultService.getByLessonPlanId(id);
        Map<String, Object> data = new HashMap<>();
        data.put("id", lessonPlan.getId());
        data.put("status", lessonPlan.getStatus());
        data.put("parseResult", parseResult);
        return new ResponseVO("success", 200, null, data);
    }

    @DeleteMapping("/delete/{id}")
    public ResponseVO delete(@PathVariable("id") Integer id) {
        lessonPlanService.deleteLessonPlanById(id);
        return new ResponseVO("success", 200, null, null);
    }
}
