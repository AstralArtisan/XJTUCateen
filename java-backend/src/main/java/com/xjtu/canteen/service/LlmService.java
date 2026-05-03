package com.xjtu.canteen.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.*;

@Service
public class LlmService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private final String apiUrl;
    private final String apiKey;
    private final String model;

    public LlmService(@Value("${LLM_API_URL:}") String apiUrl,
                      @Value("${LLM_API_KEY:}") String apiKey,
                      @Value("${LLM_MODEL:}") String model,
                      @Value("${DEEPSEEK_API_KEY:}") String deepseekApiKey,
                      @Value("${DEEPSEEK_MODEL:deepseek-chat}") String deepseekModel) {
        this.apiUrl = apiUrl == null || apiUrl.isBlank() ? "https://api.deepseek.com/v1/chat/completions" : normalizeChatCompletionsUrl(apiUrl);
        this.apiKey = apiKey == null || apiKey.isBlank() ? deepseekApiKey : apiKey;
        this.model = model == null || model.isBlank() ? deepseekModel : model;
    }

    public Map<String, Object> recommendWithDeepseek(String preferenceText, String category, List<Map<String, Object>> candidates, Map<String, Object> userContext) {
        if (candidates == null || candidates.isEmpty()) {
            return fallback(List.of(), "这个条件下没找到合适的窗口，换个筛选试试？");
        }
        if (apiKey == null || apiKey.isBlank()) {
            return fallback(candidates, "AI 助手暂时不在线，先给你看看口碑不错的几家。");
        }

        try {
            List<Map<String, Object>> compact = candidates.stream().limit(6).toList();
            Map<String, Object> userPayload = new LinkedHashMap<>();
            userPayload.put("category", category == null ? "" : category);
            userPayload.put("preference_text", preferenceText == null ? "" : preferenceText);
            userPayload.put("candidate_windows", compact);
            userPayload.put("user_context", userContext == null ? Map.of() : userContext);

            String systemPrompt = "你是西安交通大学食堂推荐助手。输出 JSON：{\"summary\":\"...\",\"tips\":\"...\",\"picked_ids\":[...]}，只从候选中选。";
            Map<String, Object> body = new LinkedHashMap<>();
            body.put("model", model);
            body.put("messages", List.of(
                Map.of("role", "system", "content", systemPrompt),
                Map.of("role", "user", "content", mapper.writeValueAsString(userPayload))
            ));
            body.put("temperature", 0.3);

            HttpRequest request = HttpRequest.newBuilder(URI.create(apiUrl))
                .timeout(Duration.ofSeconds(30))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 400) {
                return fallback(candidates, "网络开小差了，先看看这几家吧。");
            }
            Map<String, Object> raw = mapper.readValue(response.body(), new TypeReference<>() {});
            List<?> choices = (List<?>) raw.getOrDefault("choices", List.of());
            if (choices.isEmpty()) return fallback(candidates, "推荐服务出了点问题，先看看这几家口碑不错的。");
            Map<?, ?> first = (Map<?, ?>) choices.get(0);
            Object messageObj = first.get("message");
            Map<?, ?> message = messageObj instanceof Map<?, ?> map ? map : Map.of();
            String content = Objects.toString(message.get("content"), "");
            Map<String, Object> parsed = extractJson(content);
            if (parsed == null) {
                return Map.of(
                    "enabled", true,
                    "source", "llm",
                    "model", model,
                    "summary", content.isBlank() ? "给你找到了几家，具体看下面的推荐。" : content,
                    "tips", "可以换个描述再试试，说得越具体推荐越准。",
                    "picked_ids", defaultIds(candidates)
                );
            }
            List<Integer> picked = new ArrayList<>();
            Object idsObj = parsed.get("picked_ids");
            if (idsObj instanceof List<?> ids) {
                for (Object id : ids) {
                    try { picked.add(Integer.parseInt(String.valueOf(id))); } catch (Exception ignored) {}
                }
            }
            if (picked.isEmpty()) picked = defaultIds(candidates);
            return Map.of(
                "enabled", true,
                "source", "llm",
                "model", model,
                "summary", Objects.toString(parsed.getOrDefault("summary", "给你挑了几家，看看合不合口味。")),
                "tips", Objects.toString(parsed.getOrDefault("tips", "口味变了随时可以重新说，我再给你找找。")),
                "picked_ids", picked.stream().limit(3).toList()
            );
        } catch (Exception e) {
            return fallback(candidates, "推荐服务出了点问题，先看看这几家口碑不错的。");
        }
    }

    private Map<String, Object> fallback(List<Map<String, Object>> candidates, String message) {
        return Map.of(
            "enabled", false,
            "source", "local",
            "model", model,
            "summary", message,
            "tips", "下面是根据评分和热度给你挑的，仅供参考。",
            "picked_ids", defaultIds(candidates)
        );
    }

    private String normalizeChatCompletionsUrl(String rawUrl) {
        String url = rawUrl.trim();
        if (url.endsWith("/")) url = url.substring(0, url.length() - 1);
        if (url.endsWith("/chat/completions")) return url;
        if (url.endsWith("/v1")) return url + "/chat/completions";
        return url + "/v1/chat/completions";
    }

    private List<Integer> defaultIds(List<Map<String, Object>> candidates) {
        return candidates.stream().limit(3).map(i -> ((Number) i.get("stall_id")).intValue()).toList();
    }

    private Map<String, Object> extractJson(String text) {
        if (text == null || text.isBlank()) return null;
        try {
            return mapper.readValue(text.trim(), new TypeReference<>() {});
        } catch (Exception ignored) {}
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            try {
                return mapper.readValue(text.substring(start, end + 1), new TypeReference<>() {});
            } catch (Exception ignored) {}
        }
        return null;
    }
}
