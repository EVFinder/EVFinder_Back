package com.example.backend.common.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import java.security.Key;
import java.time.Instant;
import java.util.Date;
import java.util.Map;

public class JwtUtil {
    private final Key key;
    public JwtUtil(String secret){ this.key = Keys.hmacShaKeyFor(secret.getBytes()); }

    public String create(String sub, Map<String,Object> claims, long ttlSeconds){
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(sub).addClaims(claims)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(ttlSeconds)))
                .signWith(key).compact();
    }
    public Jws<Claims> parse(String token){ return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token); }
}
