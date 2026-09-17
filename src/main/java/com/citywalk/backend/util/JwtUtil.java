package com.citywalk.backend.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    @Value("${citywalk.jwt.secret}")
    private String secret;

    @Value("${citywalk.jwt.expiration}")
    private Long expiration;
    @PostConstruct
    public void init() {
        System.out.println("===== JWT secret 长度：" + (secret != null ? secret.length() : "null"));
        System.out.println("===== JWT expiration：" + expiration);
    }

    private SecretKey getKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 生成 token
     */
    public String generateToken(Long userId, String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getKey())
                .compact();
    }

    /**
     * 解析 token，拿到 userId
     */
    public Long getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Long.parseLong(claims.getSubject());
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 校验 token 是否有效
     */
    public boolean validateToken(String token) {
        return getUserIdFromToken(token) != null;
    }
}