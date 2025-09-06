package com.example.backend.auth.dto;

public class LoginResponse {
    private String uid;
    private String email;
    private String jwt;

    public LoginResponse() {}

    public LoginResponse(String uid, String email, String jwt) {
        this.uid = uid;
        this.email = email;
        this.jwt = jwt;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getJwt() {
        return jwt;
    }

    public void setJwt(String jwt) {
        this.jwt = jwt;
    }
}