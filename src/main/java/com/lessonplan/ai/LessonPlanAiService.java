package com.lessonplan.ai;

import com.lessonplan.entity.dto.LessonPlanParseDto;

/**
 * AI解析服务接口
 * 当前仅提供Mock实现，后续可扩展接入llama.cpp等本地模型
 */
public interface LessonPlanAiService {

    /**
     * 解析教案文本内容，返回结构化结果
     */
    LessonPlanParseDto parse(String content);

    /**
     * 获取当前使用的模型名称
     */
    String getModelName();

    /**
     * 是否为mock实现
     */
    boolean isMock();
}
