package com.example.hrms.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtUtil {
    
    private final RsaKeyUtil rsaKeyUtil;

    @Value("${jwt.expiration-ms:86400000}")
    private long jwtExpirationMs;

    public String generateToken(String username) {
        RSAPrivateKey privateKey = rsaKeyUtil.getPrivateKey();
        
        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationMs))
                .signWith(privateKey)
                .compact();
    }

    public String getUsernameFromToken(String token) {
        RSAPublicKey publicKey = rsaKeyUtil.getPublicKey();
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            RSAPublicKey publicKey = rsaKeyUtil.getPublicKey();
            Jwts.parserBuilder()
                    .setSigningKey(publicKey)
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Date getExpirationDateFromToken(String token) {
        RSAPublicKey publicKey = rsaKeyUtil.getPublicKey();
        
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
        
        return claims.getExpiration();
    }
}
