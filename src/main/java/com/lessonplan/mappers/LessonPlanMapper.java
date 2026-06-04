package com.lessonplan.mappers;

import com.lessonplan.entity.po.LessonPlan;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface LessonPlanMapper {
    List<LessonPlan> selectList(@Param("offset") int offset, @Param("limit") int limit);
    Integer selectCount();
    Integer insert(@Param("bean") LessonPlan bean);
    LessonPlan selectById(@Param("id") Integer id);
    Integer updateById(@Param("bean") LessonPlan bean, @Param("id") Integer id);
    Integer deleteById(@Param("id") Integer id);
}
