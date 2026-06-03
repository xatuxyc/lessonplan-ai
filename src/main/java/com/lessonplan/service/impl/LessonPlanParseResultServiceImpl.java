package com.lessonplan.service.impl;

import com.lessonplan.entity.po.LessonPlanParseResult;
import com.lessonplan.mappers.LessonPlanParseResultMapper;
import com.lessonplan.service.LessonPlanParseResultService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

/**
 * AI解析结果Service实现
 */
@Service("lessonPlanParseResultService")
public class LessonPlanParseResultServiceImpl implements LessonPlanParseResultService {

    @Resource
    private LessonPlanParseResultMapper lessonPlanParseResultMapper;

    @Override
    public LessonPlanParseResult getByLessonPlanId(Integer lessonPlanId) {
        return lessonPlanParseResultMapper.selectByLessonPlanId(lessonPlanId);
    }

    @Override
    public Integer add(LessonPlanParseResult bean) {
        return lessonPlanParseResultMapper.insert(bean);
    }

    @Override
    public Integer updateByLessonPlanId(LessonPlanParseResult bean, Integer lessonPlanId) {
        return lessonPlanParseResultMapper.updateByLessonPlanId(bean, lessonPlanId);
    }
}
