package com.example.backend.auth.dto;

public class LoginResponse {
    private String uid;
    private String email;
    private String userName;
    private String jwt;

    public LoginResponse() {}

    public LoginResponse(String uid, String email,String userName, String jwt) {
        this.uid = uid;
        this.email = email;
        this.userName = userName;
        this.jwt = jwt;
    }

    public String getUid() {return uid;}
    public void setUid(String uid) {this.uid = uid;}

    public String getEmail() {return email;}
    public void setEmail(String email) {this.email = email;}

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getJwt() {return jwt;}
    public void setJwt(String jwt) {this.jwt = jwt;}
}