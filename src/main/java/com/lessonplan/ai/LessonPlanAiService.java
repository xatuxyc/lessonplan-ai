package com.lessonplan.ai;

import com.lessonplan.entity.dto.LessonPlanParseDto;

/**
 * AI解析服务接口
 * 支持多种实现：Ollama本地模型 / DashScope云模型 / Mock
 */
public interface LessonPlanAiService {

    /**
     * 解析教案文本内容，返回结构化结果
     *
     * @param content 教案文本内容
     * @return 解析结果
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
