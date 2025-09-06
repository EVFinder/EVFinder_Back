package com.example.backend.common.util;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration-ms}")
    private long expirationMs;

    //서명 키
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    //토큰 발급
    public String generateToken(String uid, String email) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        Key key = Keys.hmacShaKeyFor(secret.getBytes());

        return Jwts.builder()
                .setSubject(uid)
                .claim("email", email)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    //토큰 검증 및 Claims추출
    public Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    //UID추출
    public String getUid(String token) {
        return parseClaims(token).getSubject(); // sub
    }

    public String getEmail(String token){
        Object email = parseClaims(token).get("email");
        return email!=null ? email.toString() : null;
    }

    //토큰 만료 여부 체크
    public boolean isTokenExpired(String token) {
        Date expiration = parseClaims(token).getExpiration();
        return expiration.before(new Date());
    }
}