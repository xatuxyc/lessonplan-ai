package com.lessonplan.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lessonplan.entity.dto.LessonPlanParseDto;
import com.lessonplan.entity.config.AppConfig;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ollama本地大模型解析实现
 * 通过HTTP调用本地Ollama服务（如 qwen2.5:7b）
 */
@Component("ollamaAiService")
public class OllamaAiServiceImpl implements LessonPlanAiService {

    private static final Logger logger = LoggerFactory.getLogger(OllamaAiServiceImpl.class);

    @Resource
    private AppConfig appConfig;

    @Override
    public LessonPlanParseDto parse(String content) {
        try {
            String prompt = buildPrompt(content);
            String response = callOllama(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            logger.error("Ollama解析失败", e);
            throw new RuntimeException("AI解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getModelName() {
        return "ollama/" + appConfig.getOllamaModel();
    }

    @Override
    public boolean isMock() {
        return false;
    }

    /**
     * 构建提示词
     */
    private String buildPrompt(String content) {
        return "你是一个教案分析专家。请分析以下教案内容，提取结构化信息。" +
                "请严格按照以下JSON格式返回，不要返回其他内容：\n" +
                "{\n" +
                "  \"title\": \"教案标题\",\n" +
                "  \"gradeLevel\": \"适用年级\",\n" +
                "  \"subject\": \"学科\",\n" +
                "  \"teachingObjectives\": [\"目标1\", \"目标2\", \"目标3\"],\n" +
                "  \"keywords\": [\"关键词1\", \"关键词2\", \"关键词3\"],\n" +
                "  \"summary\": \"100字以内摘要\"\n" +
                "}\n\n" +
                "要求：\n" +
                "1. teachingObjectives 提供3-5个教学目标\n" +
                "2. keywords 提供3-5个关键词\n" +
                "3. summary 控制在100字以内\n\n" +
                "教案内容：\n" + content;
    }

    /**
     * 调用Ollama HTTP API
     */
    private String callOllama(String prompt) throws Exception {
        String baseUrl = appConfig.getOllamaBaseUrl();
        String model = appConfig.getOllamaModel();

        URL url = new URL(baseUrl + "/api/generate");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", model);
        requestBody.put("prompt", prompt);
        requestBody.put("stream", false);

        String jsonBody = JSON.toJSONString(requestBody);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        int responseCode = conn.getResponseCode();
        if (responseCode != 200) {
            throw new RuntimeException("Ollama返回错误码: " + responseCode);
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // Ollama返回格式: {"response": "...", "done": true}
        Map<String, Object> result = JSON.parseObject(response.toString(), Map.class);
        return (String) result.get("response");
    }

    /**
     * 解析AI返回的JSON
     */
    private LessonPlanParseDto parseResponse(String response) {
        // 尝试从返回文本中提取JSON
        String jsonStr = response.trim();
        int startIdx = jsonStr.indexOf("{");
        int endIdx = jsonStr.lastIndexOf("}");
        if (startIdx >= 0 && endIdx > startIdx) {
            jsonStr = jsonStr.substring(startIdx, endIdx + 1);
        }

        Map<String, Object> map = JSON.parseObject(jsonStr, Map.class);
        LessonPlanParseDto dto = new LessonPlanParseDto();
        dto.setTitle((String) map.get("title"));
        dto.setGradeLevel((String) map.get("gradeLevel"));
        dto.setSubject((String) map.get("subject"));
        dto.setSummary((String) map.get("summary"));

        // 解析教学目标数组
        Object objList = map.get("teachingObjectives");
        if (objList instanceof JSONArray) {
            List<String> objectives = new ArrayList<>();
            for (Object obj : (JSONArray) objList) {
                objectives.add(obj.toString());
            }
            dto.setTeachingObjectives(objectives);
        }

        // 解析关键词数组
        objList = map.get("keywords");
        if (objList instanceof JSONArray) {
            List<String> keywords = new ArrayList<>();
            for (Object obj : (JSONArray) objList) {
                keywords.add(obj.toString());
            }
            dto.setKeywords(keywords);
        }

        return dto;
    }
}
