package com.example.backend.chatbot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class ChatBotGptService {

    @Value("${gpt.api.key}")
    private String apiKey;

    @Value("${gpt.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGpt(String userMessage) {
        String url = "https://api.openai.com/v1/chat/completions";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> requestBody = Map.of(
                "model", model,
                "messages", new Object[]{
                        Map.of("role", "system", "content",
                            "너는 EVFinder 전기차 안내 챗봇이다. "
                            + "'전기차 충전소 어디있어?' 또는 비슷한 질문이 들어오면 반드시 "
                            + "'EVFinder 지도 화면에서 검색 또는 지도를 움직여 확인할 수 있습니다!' 라고 답해야 한다."),
                        Map.of("role", "user", "content", userMessage)
                }
        );

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                Map.class
        );

        if (response.getBody() != null && response.getBody().containsKey("choices")) {
            var choices = (List<Map<String, Object>>) response.getBody().get("choices");
            if (!choices.isEmpty()) {
                var message = (Map<String, Object>) choices.get(0).get("message");
                return (String) message.get("content");
            }
        }
        return "GPT 응답을 가져올 수 없습니다.";
    }

}
