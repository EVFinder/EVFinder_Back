package com.example.backend.chatbot.controller;

import com.example.backend.chatbot.dto.ChatBotRequestDTO;
import com.example.backend.chatbot.dto.ChatBotResponseDTO;
import com.example.backend.chatbot.dto.ChatBotMessageDTO;
import com.example.backend.chatbot.service.ChatBotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chatbot")
@RequiredArgsConstructor
public class ChatBotController {

    private final ChatBotService chatBotService;

    @PostMapping("/ask")
    public ResponseEntity<ChatBotResponseDTO> ask(@RequestBody ChatBotRequestDTO request) {
        // conversationId가 비어있으면 서버가 생성
        String conversationId = (request.getConversationId() == null || request.getConversationId().isBlank())
                ? chatBotService.createConversationId()
                : request.getConversationId();

        String answer = chatBotService.handleMessage(
                request.getUid(),
                conversationId,
                request.getMessage()
        );

        return ResponseEntity.ok(new ChatBotResponseDTO(conversationId, answer));
    }

    @GetMapping("/history")
    public ResponseEntity<List<ChatBotMessageDTO>> history(
            @RequestParam String uid,
            @RequestParam String conversationId) throws Exception {
        return ResponseEntity.ok(chatBotService.getHistory(uid, conversationId));
    }
}
