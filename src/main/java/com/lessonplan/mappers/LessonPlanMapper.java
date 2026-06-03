package com.lessonplan.mappers;

import com.lessonplan.entity.po.LessonPlan;
import com.lessonplan.entity.query.LessonPlanQuery;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 教案文件Mapper
 */
public interface LessonPlanMapper {

    Integer insert(@Param("bean") LessonPlan bean);

    Integer insertOrUpdate(@Param("bean") LessonPlan bean);

    List<LessonPlan> selectList(@Param("query") LessonPlanQuery query);

    Integer selectCount(@Param("query") LessonPlanQuery query);

    LessonPlan selectById(@Param("id") Integer id);

    Integer updateById(@Param("bean") LessonPlan bean, @Param("id") Integer id);

    Integer deleteById(@Param("id") Integer id);
}
