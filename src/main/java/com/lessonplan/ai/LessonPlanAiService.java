package com.lessonplan.ai;

import com.lessonplan.entity.dto.LessonPlanParseDto;

import java.util.Arrays;

public class LessonPlanAiService {

    public LessonPlanParseDto parse(String content) {
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        LessonPlanParseDto dto = new LessonPlanParseDto();
        dto.setTitle("【Mock】教案标题 - 基于内容的模拟解析");
        dto.setGradeLevel("小学五年级");
        dto.setSubject("语文");
        dto.setTeachingObjectives(Arrays.asList(
                "理解课文主要内容，体会作者表达的思想感情",
                "掌握本课生字新词，能正确读写并运用",
                "学习文章的写作手法，提高写作能力",
                "培养阅读兴趣和良好的阅读习惯"
        ));
        dto.setKeywords(Arrays.asList("阅读理解", "写作手法", "生字新词", "思想感情", "阅读习惯"));
        dto.setSummary("本教案围绕课文阅读理解展开，通过生字词教学、内容分析、写作手法学习等环节，帮助学生深入理解课文，提升语文综合素养。");
        return dto;
    }

    public String getModelName() {
        return "mock/qwen2.5-mock";
    }

    public boolean isMock() {
        return true;
    }
}
