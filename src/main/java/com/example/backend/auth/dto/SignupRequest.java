package com.example.backend.auth.dto;

public class SignupRequest {
    private String email;
    private String password;
    private String userName;

    public SignupRequest() {}

    public SignupRequest(String email, String password, String userName) {
        this.email = email;
        this.password = password;
        this.userName = userName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }
}