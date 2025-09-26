package com.example.backend.chatbot.dto;

import lombok.Data;

@Data
public class ChatBotRequestDTO {
    private String uid;            // 유저 ID
    private String conversationId; // 대화방 ID
    private String message;        // 사용자 입력 메시지
}
