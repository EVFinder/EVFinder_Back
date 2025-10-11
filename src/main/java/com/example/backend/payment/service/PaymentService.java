package com.example.backend.payment.service;

import com.example.backend.payment.util.KakaoPayClient;
import com.example.backend.payment.dto.PaymentDTO;
import com.example.backend.payment.util.PaymentUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.cloud.firestore.QuerySnapshot;
import com.google.cloud.firestore.Query;

import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class PaymentService {

    //@Value("${kakao.api.secret-key}")
    //private String secretKey;

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
            "http://100.100.101.97:8080/success.html?status=success", // 실제 서버 ip주소로 변경 필요
            "http://100.100.101.97:8080/cancel.html?status=cancel",
            "http://100.100.101.97:8080/fail.html?status=fail"
        );

        String tid = (String) kakaoResponse.get("tid");

        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("paymentId", tid);
        paymentData.put("orderId", orderId);
        paymentData.put("reserveId", dto.getReserveId());
        paymentData.put("itemName", dto.getItemName());
        paymentData.put("amount", dto.getAmount());
        paymentData.put("status", "READY");
        paymentData.put("createdAt", LocalDateTime.now().toString());
        paymentUtil.savePayment(dto.getUid(), paymentData);

        Map<String, Object> response = new HashMap<>();
        response.put("tid", tid);
        response.put("uid", dto.getUid());
        response.put("orderId", orderId);
        response.put("reserveId", dto.getReserveId());
        response.put("next_redirect_mobile_url", kakaoResponse.get("next_redirect_mobile_url"));

        return response;
    }

    // 결제 승인
    public Map<String, Object> approvePayment(String uid, String orderId, String tid, String pgToken) throws Exception {
        // 승인 요청
        Map<String, Object> approvalResponse = kakaoPayClient.approvePayment(tid, uid, orderId, pgToken);

        // 결제 데이터 생성
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("paymentId", tid);               // Firestore 문서명
        paymentData.put("orderId", orderId);             // 주문 번호
        paymentData.put("status", "SUCCESS");            // 상태
        paymentData.put("approvedAt", LocalDateTime.now().toString()); // 승인 시간

        // uid null 체크
        if (uid == null || uid.isEmpty()) {
            throw new IllegalArgumentException("UID가 유효하지 않습니다.");
        }

        // Firestore에 저장 (users/{uid}/payments/{paymentId})
        paymentUtil.savePayment(uid, paymentData);

        // 카카오 응답에도 메시지 추가
        approvalResponse.put("message", "결제 성공");
        return approvalResponse;
    }

    // 결제 취소
    public Map<String, Object> cancelPayment(String uid, String tid) throws Exception {

        Firestore db = paymentUtil.getFirestore();

        // Firestore에서 결제금액 조회
        DocumentSnapshot paymentDoc = db.collection("users")
                .document(uid)
                .collection("payments")
                .document(tid)
                .get()
                .get();

        if (!paymentDoc.exists()) {
            throw new IllegalArgumentException("결제내역이 존재하지 않습니다.");
        }

        Number amountNum = (Number) paymentDoc.get("amount");
        int amount = amountNum.intValue();

        if (amount <= 0) {
            throw new IllegalArgumentException("결제 금액이 유효하지 않습니다.");
        }

        // 카카오페이 결제취소 요청
        Map<String, Object> kakaoResponse = kakaoPayClient.cancelPayment(tid, amount);

        // Firestore 상태 갱신
        Map<String, Object> paymentData = new HashMap<>();
        paymentData.put("paymentId", tid);
        paymentData.put("status", "CANCELLED");
        paymentData.put("cancelledAt", LocalDateTime.now().toString());
        paymentUtil.savePayment(uid, paymentData);

        return Map.of(
                "message", "결제 취소 완료",
                "status", "CANCELLED",
                "cancel_amount", amount
        );
    }

    // 결제 내역 조회
    public List<Map<String, Object>> getPaymentHistory(String uid)
            throws ExecutionException, InterruptedException {

        Firestore db = paymentUtil.getFirestore();

        // 해당 사용자의 모든 결제 문서 조회
        ApiFuture<QuerySnapshot> future = db.collection("users")
                .document(uid)
                .collection("payments")
                .orderBy("createdAt", Query.Direction.DESCENDING) // 최신순 정렬
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Map<String, Object>> result = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            Map<String, Object> payment = new HashMap<>();
            payment.put("paymentId", doc.getString("paymentId"));
            payment.put("reserveId", doc.getString("reserveId"));
            payment.put("orderId", doc.getString("orderId"));
            payment.put("itemName", doc.getString("itemName"));
            payment.put("amount", doc.get("amount"));
            payment.put("status", doc.getString("status"));
            payment.put("createdAt", doc.getString("createdAt"));
            payment.put("approvedAt", doc.getString("approvedAt"));
            payment.put("cancelledAt", doc.getString("cancelledAt"));
            result.add(payment);
        }

        return result;
    }

}
