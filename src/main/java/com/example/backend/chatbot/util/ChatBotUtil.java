package com.example.backend.chatbot.util;

import com.example.backend.chatbot.dto.ChatBotMessageDTO;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ChatBotUtil {

    private final Firestore firestore;

    // 메시지 저장
    public void saveMessage(String uid, String conversationId, ChatBotMessageDTO message) {
        firestore.collection("users")
                .document(uid)
                .collection("chats")
                .document(conversationId)
                .collection("messages")
                .add(message);
    }

    // 메시지 조회
    public List<ChatBotMessageDTO> getMessages(String uid, String conversationId)
            throws ExecutionException, InterruptedException {
        ApiFuture<QuerySnapshot> future = firestore.collection("users")
                .document(uid)
                .collection("chats")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .get();

        return future.get().getDocuments()
                .stream()
                .map(doc -> doc.toObject(ChatBotMessageDTO.class))
                .collect(Collectors.toList());
    }
}
