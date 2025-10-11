package com.example.backend.payment.controller;

import com.example.backend.payment.dto.PaymentDTO;
import com.example.backend.payment.service.PaymentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/payment")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // 결제 요청
    @PostMapping("/request")
    public ResponseEntity<Map<String, Object>> requestPayment(@RequestBody PaymentDTO dto)
            throws ExecutionException, InterruptedException {
        Map<String, Object> result = paymentService.requestPayment(dto);
        return ResponseEntity.ok(result);
    }

    // 결제 승인
    @PostMapping("/approve")
    public ResponseEntity<Map<String, Object>> approvePayment(@RequestBody Map<String, Object> body) throws Exception {
        String uid = (String) body.get("uid");
        String orderId = (String) body.get("orderId");
        String tid = (String) body.get("tid");
        String pgToken = (String) body.get("pg_token");

        Map<String, Object> result = paymentService.approvePayment(uid, orderId, tid, pgToken);
        return ResponseEntity.ok(result);
    }

    // 결제 취소
    @GetMapping("/cancel")
    public ResponseEntity<Map<String, Object>> cancelPayment(@RequestParam("uid") String uid,
                                                             @RequestParam("tid") String tid) throws Exception {
        Map<String, Object> result = paymentService.cancelPayment(uid, tid);
        return ResponseEntity.ok(result);
    }

    // 결제 내역 조회
    @GetMapping("/history")
    public ResponseEntity<?> getPaymentHistory(@RequestParam("uid") String uid)
            throws ExecutionException, InterruptedException {
        List<Map<String, Object>> history = paymentService.getPaymentHistory(uid);
        return ResponseEntity.ok(history);
    }
    
}
