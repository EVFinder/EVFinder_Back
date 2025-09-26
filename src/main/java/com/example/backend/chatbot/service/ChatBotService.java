package com.example.backend.chatbot.service;

import com.example.backend.chatbot.dto.ChatBotMessageDTO;
import com.example.backend.chatbot.util.ChatBotUtil;
import com.google.cloud.Timestamp;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatBotService {

    private final ChatBotGptService gptService;
    private final ChatBotUtil chatBotUtil;

    public String handleMessage(String uid, String conversationId, String userMessage) {
        Timestamp now = Timestamp.now();
        Timestamp expireAt = Timestamp.ofTimeSecondsAndNanos(
            now.getSeconds() + 60L * 60 * 24 * 3, 0 // 3일 후
        );  
        // 1. 사용자 메시지 저장
        ChatBotMessageDTO userMsg = new ChatBotMessageDTO("user", userMessage, now, expireAt);
        chatBotUtil.saveMessage(uid, conversationId, userMsg);

        // 2. GPT 호출
        String answer = gptService.askGpt(userMessage);

        // 3. GPT 응답 저장
        ChatBotMessageDTO botMsg = new ChatBotMessageDTO("assistant", answer, now, expireAt);
        chatBotUtil.saveMessage(uid, conversationId, botMsg);

        return answer;
    }
    // 새 conversationId 생성
    public String createConversationId() {
        return "c-" + UUID.randomUUID();
    }

    // 대화 내역 조회
    public List<ChatBotMessageDTO> getHistory(String uid, String conversationId) throws Exception {
        return chatBotUtil.getMessages(uid, conversationId);
    }

}
