package com.lessonplan.service;

import com.lessonplan.entity.po.LessonPlanParseResult;

/**
 * AI解析结果Service
 */
public interface LessonPlanParseResultService {

    LessonPlanParseResult getByLessonPlanId(Integer lessonPlanId);

    Integer add(LessonPlanParseResult bean);

    Integer updateByLessonPlanId(LessonPlanParseResult bean, Integer lessonPlanId);
}
