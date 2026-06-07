package com.lessonplan.ai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.lessonplan.entity.dto.LessonPlanParseDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 * llama.cpp 本地模型解析实现（备份，未启用）
 *
 * 使用方式：
 * 1. 启动 llama.cpp server: ./llama-server -m model.gguf --port 8080
 * 2. 在 application.properties 中设置 ai.service.mode=llamacpp
 * 3. 将此类加上 @Component 注解，并移除 MockAiServiceImpl 的 @Component
 *
 * llama.cpp 兼容 OpenAI API 格式，调用 /v1/chat/completions 接口
 */
// @Component  // 取消注释以启用
public class LlamaCppAiServiceImpl implements LessonPlanAiService {

    private static final Logger logger = LoggerFactory.getLogger(LlamaCppAiServiceImpl.class);

    private static final String DEFAULT_BASE_URL = "http://localhost:8080";
    private static final String DEFAULT_MODEL = "qwen2.5-7b";

    @Override
    public LessonPlanParseDto parse(String content) {
        try {
            String prompt = buildPrompt(content);
            String response = callLlamaCpp(prompt);
            return parseResponse(response);
        } catch (Exception e) {
            logger.error("llama.cpp解析失败", e);
            throw new RuntimeException("AI解析失败: " + e.getMessage(), e);
        }
    }

    @Override
    public String getModelName() {
        return "llamacpp/" + DEFAULT_MODEL;
    }

    @Override
    public boolean isMock() {
        return false;
    }

    private String buildPrompt(String content) {
        return "你是一个教案分析专家。请分析以下教案内容，提取结构化信息。" +
                "请严格按照以下JSON格式返回，不要返回其他内容：\n" +
                "{\"title\":\"教案标题\",\"gradeLevel\":\"适用年级\",\"subject\":\"学科\"," +
                "\"teachingObjectives\":[\"目标1\",\"目标2\",\"目标3\"]," +
                "\"keywords\":[\"关键词1\",\"关键词2\",\"关键词3\"]," +
                "\"summary\":\"100字以内摘要\"}\n\n" +
                "要求：teachingObjectives 3-5个，keywords 3-5个，summary 100字以内。\n\n" +
                "教案内容：\n" + content;
    }

    private String callLlamaCpp(String prompt) throws Exception {
        URL url = new URL(DEFAULT_BASE_URL + "/v1/chat/completions");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);
        conn.setConnectTimeout(30000);
        conn.setReadTimeout(120000);

        Map<String, Object> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", DEFAULT_MODEL);
        requestBody.put("messages", List.of(message));
        requestBody.put("temperature", 0.3);

        String jsonBody = JSON.toJSONString(requestBody);
        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }

        if (conn.getResponseCode() != 200) {
            throw new RuntimeException("llama.cpp返回错误码: " + conn.getResponseCode());
        }

        StringBuilder response = new StringBuilder();
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                response.append(line);
            }
        }

        // 解析 OpenAI 格式响应
        Map<String, Object> result = JSON.parseObject(response.toString(), Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) result.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> messageMap = (Map<String, Object>) choices.get(0).get("message");
            return (String) messageMap.get("content");
        }
        throw new RuntimeException("llama.cpp返回格式异常");
    }

    private LessonPlanParseDto parseResponse(String response) {
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

        Object objList = map.get("teachingObjectives");
        if (objList instanceof JSONArray) {
            List<String> objectives = new ArrayList<>();
            for (Object obj : (JSONArray) objList) {
                objectives.add(obj.toString());
            }
            dto.setTeachingObjectives(objectives);
        }

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
