package com.example.backend.chatbot.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ChatBotResponseDTO {
    private String conversationId; // 서버가 자동 생성해주는 대화방 ID
    private String answer;         // GPT 응답
}
