package com.example.backend.auth.controller;

import com.example.backend.auth.dto.LoginRequest;
import com.example.backend.auth.dto.LoginResponse;
import com.example.backend.auth.dto.RoleResponse;
import com.example.backend.auth.dto.SignupRequest;
import com.example.backend.auth.dto.SignupResponse;
import com.example.backend.auth.service.AuthService;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // 회원가입
    @PostMapping("/signup")
    public ResponseEntity<SignupResponse> signup(@RequestBody SignupRequest request) {
        try {
            SignupResponse response = authService.signup(request);
            return ResponseEntity.ok(response);
        }
        //이미 가입된 사용자
        catch (IllegalStateException e){
            return ResponseEntity.status(409).body(null);
        }
        //그 외 에러
        catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.badRequest().build();
        }
    }

    // 로그인
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 잘못된 이메일/비밀번호 -> 인증 실패
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e.getMessage()); // 401
        } catch (RuntimeException e) {
            // 서버 내부 오류
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("로그인 처리 중 오류 발생");
        }
    }

    // 구글 로그인
    @PostMapping("/google")
    public ResponseEntity<LoginResponse> googleLogin(@RequestBody Map<String, String> request) {
        try {
            String idToken = request.get("idToken");
            LoginResponse response = authService.googleLogin(idToken);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
    
    //역할 가져오기
    @GetMapping("/role")
    public ResponseEntity<RoleResponse> getRole(@RequestHeader("Authorization") String authHeader) {
        try {
            String token = authHeader.replace("Bearer ", "").trim();
            String role = authService.getRoleFromToken(token);
            return ResponseEntity.ok(new RoleResponse(role));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    // 비밀번호 재설정 - 로그인 못 하는 사용자가 이메일 통해 재설정하는 기능
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody Map<String, String> request) {
        try {
            String email = request.get("email");
            String link = authService.resetPassword(email);
            return ResponseEntity.ok(Map.of("resetLink", link));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Reset failed");
        }
    }

    // 비밀번호 변경 - 로그인된 사용자가 자기 계정 비번을 바꾸는 기능
    @PostMapping("/update-password")
    public ResponseEntity<?> updatePassword(@RequestBody Map<String, String> request) {
        try {
            String uid = request.get("uid");
            String newPassword = request.get("newPassword");
            authService.updatePassword(uid, newPassword);
            return ResponseEntity.ok("Password updated");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Update failed");
        }
    }
}
