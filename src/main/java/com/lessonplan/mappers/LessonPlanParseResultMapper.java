package com.lessonplan.mappers;

import com.lessonplan.entity.po.LessonPlanParseResult;
import org.apache.ibatis.annotations.Param;

/**
 * AI解析结果Mapper
 */
public interface LessonPlanParseResultMapper {

    Integer insert(@Param("bean") LessonPlanParseResult bean);

    LessonPlanParseResult selectByLessonPlanId(@Param("lessonPlanId") Integer lessonPlanId);

    LessonPlanParseResult selectById(@Param("id") Integer id);

    Integer updateByLessonPlanId(@Param("bean") LessonPlanParseResult bean, @Param("lessonPlanId") Integer lessonPlanId);

    Integer deleteByLessonPlanId(@Param("lessonPlanId") Integer lessonPlanId);
}
