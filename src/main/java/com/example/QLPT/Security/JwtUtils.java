package com.example.QLPT.Security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

@Component
public class JwtUtils {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private int jwtExpirationMs;

    // JWT 0.12.x yêu cầu dùng SecretKey thay vì Key chung chung
    private SecretKey key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Tạo token từ thông tin User
    public String generateJwtToken(Authentication authentication) {
        UserDetails userPrincipal = (UserDetails) authentication.getPrincipal();
        return Jwts.builder()
                .subject(userPrincipal.getUsername()) // Đổi từ setSubject thành subject
                .issuedAt(new Date())                 // Đổi từ setIssuedAt thành issuedAt
                .expiration(new Date((new Date()).getTime() + jwtExpirationMs)) // Đổi từ setExpiration
                .signWith(key())                      // Không cần truyền thêm thuật toán HS256 nữa
                .compact();
    }

    // Lấy email từ token
    public String getUserNameFromJwtToken(String token) {
        return Jwts.parser()                          // Đổi từ parserBuilder() thành parser()
                .verifyWith(key())                    // Đổi từ setSigningKey() thành verifyWith()
                .build()
                .parseSignedClaims(token)             // Đổi từ parseClaimsJws()
                .getPayload()                         // Đổi từ getBody()
                .getSubject();
    }

    // Kiểm tra token có hợp lệ không
    public boolean validateJwtToken(String authToken) {
        try {
            Jwts.parser()
                    .verifyWith(key())
                    .build()
                    .parseSignedClaims(authToken);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Lỗi JWT Token: " + e.getMessage());
        }
        return false;
    }
}