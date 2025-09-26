package com.example.backend.auth.service;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.LoginResponse;
import com.example.backend.auth.dto.SignupRequest;
import com.example.backend.auth.dto.SignupResponse;
import com.example.backend.common.util.JwtUtil;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseToken;
import com.google.firebase.auth.UserRecord;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import com.google.cloud.Timestamp;

@Service
public class AuthService {

    private final JwtUtil jwtUtil;
    private final Firestore firestore;       // Firestore Bean
    private final FirebaseAuth firebaseAuth; // FirebaseAuth Bean

    public AuthService(JwtUtil jwtUtil, Firestore firestore, FirebaseAuth firebaseAuth) {
        this.jwtUtil = jwtUtil;
        this.firestore = firestore;
        this.firebaseAuth = firebaseAuth;
    }

    // 회원가입: Firestore에 사용자 문서 생성 후 JWT 발급
    public SignupResponse signup(SignupRequest request) throws Exception {
        try {
            // Firebase Auth에 사용자 생성
            UserRecord.CreateRequest createRequest = new UserRecord.CreateRequest()
                    .setEmail(request.getEmail())
                    .setPassword(request.getPassword());

            UserRecord userRecord = firebaseAuth.createUser(createRequest);

            String uid = userRecord.getUid();
            String email = userRecord.getEmail();
            String role = "USER";

            DocumentReference userRef = firestore.collection("users").document(uid);

            // 이미 가입된 경우 예외 처리
            if (userRef.get().get().exists()) {
                throw new IllegalStateException("이미 가입된 사용자입니다.");
            }

            Map<String, Object> userData = new HashMap<>();
            userData.put("email", email);
            userData.put("userName", request.getUserName());
            userData.put("createdAt", Timestamp.now());
            userData.put("role", role);

            //찍고, .get으로 가져오기
            userRef.set(userData).get();

            String jwt = jwtUtil.generateToken(uid, email, request.getUserName(), role);

            return new SignupResponse(uid, email, request.getUserName(), jwt);

        } catch (Exception e) {
            System.err.println("Firebase Signup 실패: " + e.getMessage());
            throw e;
        }
    }

    // 로그인: Firebase ID 토큰 검증 → JWT 발급
    public LoginResponse login(LoginRequest request) throws Exception {
        try {
            // 1. Firebase REST API 호출
            String url = "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key="
                    + "AIzaSyA2ma0KaWqvOdjq8FU9qanbXeNi2ocilwo";

            RestTemplate restTemplate = new RestTemplate();

            Map<String, Object> body = new HashMap<>();
            body.put("email", request.getEmail());
            body.put("password", request.getPassword());
            body.put("returnSecureToken", true);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, body, Map.class);

            if (response.getStatusCode() != HttpStatus.OK) {
                throw new IllegalArgumentException("로그인 실패: Firebase 응답 오류");
            }

            Map<String, Object> responseBody = response.getBody();
            if (responseBody == null || !responseBody.containsKey("idToken")) {
                throw new IllegalArgumentException("로그인 실패: 잘못된 Firebase 응답");
            }

            String idToken = (String) responseBody.get("idToken");

            FirebaseToken decodedToken = firebaseAuth.verifyIdToken(idToken);
            String uid = decodedToken.getUid();
            String email = decodedToken.getEmail();

            // 2. Firestore에서 userName 가져오기 (Bean 사용)
            DocumentSnapshot snapshot = firestore.collection("users").document(uid).get().get();
            String userName = snapshot.contains("userName") ? snapshot.getString("userName") : "사용자";
            String role = snapshot.contains("role") ? snapshot.getString("role") : "USER";

            // 3. JWT 발급
            String jwt = jwtUtil.generateToken(uid, email,userName, role);

            return new LoginResponse(uid, email, userName, jwt);

        } catch (IllegalArgumentException e) {
            System.err.println("로그인 실패: " + e.getMessage());
            throw e;
        } catch (Exception e) {
            System.err.println("로그인 처리 중 서버 오류: " + e.getMessage());
            throw new RuntimeException("로그인 처리 중 서버 오류", e);
        }
    }
}