package com.example.backend.payment.service;

import com.example.backend.payment.util.KakaoPayClient;
import com.example.backend.payment.dto.PaymentDTO;
import com.example.backend.payment.util.PaymentUtil;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    private final KakaoPayClient kakaoPayClient;
    private final PaymentUtil paymentUtil;

    public PaymentService(KakaoPayClient kakaoPayClient, PaymentUtil paymentUtil) {
        this.kakaoPayClient = kakaoPayClient;
        this.paymentUtil = paymentUtil;
    }

    // 결제 요청
    public Map<String, Object> requestPayment(PaymentDTO dto)
            throws ExecutionException, InterruptedException {

        if (!paymentUtil.isUserExists(dto.getUid())) {
            throw new IllegalArgumentException("존재하지 않는 UID입니다.");
        }

        String orderId = "ORDER-" + LocalDateTime.now().toLocalDate() + "-" +
                UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        Map<String, Object> kakaoResponse = kakaoPayClient.requestPayment(
            dto.getUid(),
            orderId,
            dto.getItemName(),
            dto.getAmount(),
            "http://100.100.101.97:8080/success.html?result=success", // 실제 서버 ip주소로 변경 필요
            "http://100.100.101.97:8080?result=cancel",
            "http://100.100.101.97:8080?result=fail"
        );

        String tid = (String) kakaoResponse.get("tid");

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("paymentId", tid);
        paymentData.put("orderId", orderId);
        paymentData.put("itemName", dto.getItemName());
        paymentData.put("amount", dto.getAmount());
        paymentData.put("status", "READY");
        paymentData.put("createdAt", LocalDateTime.now().toString());
        paymentUtil.savePayment(dto.getUid(), paymentData);

        Map<String, Object> response = new HashMap<>();
        response.put("tid", tid);
        response.put("uid", dto.getUid());
        response.put("orderId", orderId);
        response.put("next_redirect_mobile_url", kakaoResponse.get("next_redirect_mobile_url"));

        return response;
    }

    // 결제 승인
    public Map<String, Object> approvePayment(String uid, String orderId, String tid, String pgToken) throws Exception {
        Map<String, Object> approvalResponse = kakaoPayClient.approvePayment(tid, uid, orderId, pgToken);

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("uid", uid);
        paymentData.put("orderId", orderId);
        paymentData.put("paymentId", tid);
        paymentData.put("status", "SUCCESS");
        paymentData.put("approvedAt", LocalDateTime.now().toString());

        paymentUtil.savePayment(uid, paymentData);

        approvalResponse.put("message", "결제 성공");
        return approvalResponse;
    }

    // 결제 취소
    public Map<String, Object> cancelPayment(String uid, String tid) throws Exception {
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("status", "CANCELLED");
        paymentUtil.savePayment(uid, paymentData);

        return Map.of("message", "결제 취소 완료", "status", "CANCELLED");
    }
}
