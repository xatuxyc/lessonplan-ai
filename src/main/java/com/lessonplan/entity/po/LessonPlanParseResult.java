package com.lessonplan.entity.po;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;

/**
 * AI解析结果PO（持久化对象）
 */
public class LessonPlanParseResult implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Integer id;

    /** 关联的教案文件ID */
    private Integer lessonPlanId;

    /** AI生成的标题 */
    private String title;

    /** 年级 */
    private String gradeLevel;

    /** 学科 */
    private String subject;

    /** 教学目标（JSON数组格式，3-5个） */
    private String teachingObjectives;

    /** 关键词（JSON数组格式，3-5个） */
    private String keywords;

    /** 100字内摘要 */
    private String summary;

    /** 解析耗时（毫秒） */
    private Long duration;

    /** 使用的模型名称 */
    private String modelName;

    /** 是否为mock结果：0-真实AI 1-mock */
    private Integer isMock;

    /** 失败原因（解析失败时记录） */
    private String failReason;

    /** 解析时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date parseTime;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getLessonPlanId() {
        return lessonPlanId;
    }

    public void setLessonPlanId(Integer lessonPlanId) {
        this.lessonPlanId = lessonPlanId;
    }

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

    public String getTeachingObjectives() {
        return teachingObjectives;
    }

    public void setTeachingObjectives(String teachingObjectives) {
        this.teachingObjectives = teachingObjectives;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public Integer getIsMock() {
        return isMock;
    }

    public void setIsMock(Integer isMock) {
        this.isMock = isMock;
    }

    public String getFailReason() {
        return failReason;
    }

    public void setFailReason(String failReason) {
        this.failReason = failReason;
    }

    public Date getParseTime() {
        return parseTime;
    }

    public void setParseTime(Date parseTime) {
        this.parseTime = parseTime;
    }

    @Override
    public String toString() {
        return "LessonPlanParseResult{" +
                "id=" + id +
                ", lessonPlanId=" + lessonPlanId +
                ", title='" + title + '\'' +
                ", gradeLevel='" + gradeLevel + '\'' +
                ", subject='" + subject + '\'' +
                ", isMock=" + isMock +
                ", duration=" + duration +
                '}';
    }
}
