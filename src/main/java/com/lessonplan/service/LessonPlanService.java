package com.lessonplan.service;

import com.lessonplan.entity.po.LessonPlan;
import com.lessonplan.entity.query.LessonPlanQuery;
import com.lessonplan.entity.vo.PaginationResultVO;

import java.util.List;

/**
 * 教案文件Service
 */
public interface LessonPlanService {

    List<LessonPlan> findListByParam(LessonPlanQuery param);

    Integer findCountByParam(LessonPlanQuery param);

    PaginationResultVO<LessonPlan> findListByPage(LessonPlanQuery param);

    LessonPlan getLessonPlanById(Integer id);

    Integer add(LessonPlan bean);

    Integer updateLessonPlanById(LessonPlan bean, Integer id);

    Integer deleteLessonPlanById(Integer id);

    /**
     * 上传教案文件
     */
    LessonPlan uploadFile(String originalFilename, String fileType, Long fileSize,
                          String uploader, java.io.File tempFile);

    /**
     * 触发AI解析
     */
    void triggerParse(Integer lessonPlanId);
}
