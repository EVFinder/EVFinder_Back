package com.example.backend.payment.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Component
public class KakaoPayClient {

    @Value("${kakao.api.secret-key}")
    private String secretKey;

    private static final String READY_URL = "https://open-api.kakaopay.com/online/v1/payment/ready";
    private static final String APPROVE_URL = "https://open-api.kakaopay.com/online/v1/payment/approve";

    // 결제 요청
    public Map<String, Object> requestPayment(String uid, String orderId, String itemName, int amount,
                                              String approvalUrl, String cancelUrl, String failUrl) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("partner_order_id", orderId);
        body.put("partner_user_id", uid);
        body.put("item_name", itemName);
        body.put("quantity", 1);
        body.put("total_amount", amount);
        body.put("tax_free_amount", 0);
        body.put("approval_url", approvalUrl);
        body.put("cancel_url", cancelUrl);
        body.put("fail_url", failUrl);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(READY_URL, entity, Map.class);
        return response.getBody();
    }

    // 결제 승인
    public Map<String, Object> approvePayment(String tid, String uid, String orderId, String pgToken) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "SECRET_KEY " + secretKey);
        headers.setContentType(MediaType.APPLICATION_JSON);

        Map<String, Object> body = new HashMap<>();
        body.put("cid", "TC0ONETIME");
        body.put("tid", tid);
        body.put("partner_order_id", orderId);
        body.put("partner_user_id", uid);
        body.put("pg_token", pgToken);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
        ResponseEntity<Map> response = restTemplate.postForEntity(APPROVE_URL, entity, Map.class);
        return response.getBody();
    }
}
