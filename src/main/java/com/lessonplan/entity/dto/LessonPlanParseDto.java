package com.lessonplan.entity.dto;

import java.io.Serializable;
import java.util.List;

/**
 * AI解析结果DTO
 */
public class LessonPlanParseDto implements Serializable {

    private static final long serialVersionUID = 1L;

    /** AI生成的标题 */
    private String title;

    /** 年级 */
    private String gradeLevel;

    /** 学科 */
    private String subject;

    /** 教学目标列表（3-5个） */
    private List<String> teachingObjectives;

    /** 关键词列表（3-5个） */
    private List<String> keywords;

    /** 100字内摘要 */
    private String summary;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public List<String> getTeachingObjectives() {
        return teachingObjectives;
    }

    public void setTeachingObjectives(List<String> teachingObjectives) {
        this.teachingObjectives = teachingObjectives;
    }

    public List<String> getKeywords() {
        return keywords;
    }

    public void setKeywords(List<String> keywords) {
        this.keywords = keywords;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
