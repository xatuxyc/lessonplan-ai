package com.lessonplan.entity.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 应用配置
 */
@Component("appConfig")
public class AppConfig {

    /** 文件存储根目录 */
    @Value("${project.folder:e:/code/files/lessonplan/}")
    private String projectFolder;

    /** AI服务开关 */
    @Value("${ai.service.enabled:true}")
    private Boolean aiServiceEnabled;

    /** AI服务模式：ollama / dashscope / mock */
    @Value("${ai.service.mode:mock}")
    private String aiServiceMode;

    /** Ollama服务地址 */
    @Value("${ai.ollama.base-url:http://localhost:11434}")
    private String ollamaBaseUrl;

    /** Ollama模型名称 */
    @Value("${ai.ollama.model:qwen2.5:7b}")
    private String ollamaModel;

    /** 文件大小限制（字节），默认50MB */
    private final Long maxFileSize = 50L * 1024 * 1024;

    public String getProjectFolder() {
        return projectFolder;
    }

    public void setProjectFolder(String projectFolder) {
        this.projectFolder = projectFolder;
    }

    public Boolean getAiServiceEnabled() {
        return aiServiceEnabled;
    }

    public void setAiServiceEnabled(Boolean aiServiceEnabled) {
        this.aiServiceEnabled = aiServiceEnabled;
    }

    public String getAiServiceMode() {
        return aiServiceMode;
    }

    public void setAiServiceMode(String aiServiceMode) {
        this.aiServiceMode = aiServiceMode;
    }

    public String getOllamaBaseUrl() {
        return ollamaBaseUrl;
    }

    public void setOllamaBaseUrl(String ollamaBaseUrl) {
        this.ollamaBaseUrl = ollamaBaseUrl;
    }

    public String getOllamaModel() {
        return ollamaModel;
    }

    public void setOllamaModel(String ollamaModel) {
        this.ollamaModel = ollamaModel;
    }

    public Long getMaxFileSize() {
        return maxFileSize;
    }
}
