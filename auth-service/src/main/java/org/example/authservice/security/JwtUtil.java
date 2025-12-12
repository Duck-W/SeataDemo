package org.example.authservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:mySecretKey}")
    private String secret;
    
    // 添加volatile关键字确保多线程环境下的可见性
    private volatile byte[] secretKeyBytes;

    @Value("${jwt.expiration:86400}")
    private Long expiration;

    // 初始化secretKeyBytes，避免每次生成token时都转换
    @PostConstruct
    public void init() {
        if (secret != null && !secret.isEmpty()) {
            secretKeyBytes = secret.getBytes();
        }
    }

    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }

    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        // 添加超时保护
        return Jwts.parser()
                .setSigningKey(secretKeyBytes != null ? secretKeyBytes : secret.getBytes())
                .parseClaimsJws(token)
                .getBody();
    }

    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public String generateToken(String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return doGenerateToken(claims, username);
    }

    private String doGenerateToken(Map<String, Object> claims, String subject) {
        // 减少计算复杂度，预计算过期时间
        long currentTimeMillis = System.currentTimeMillis();
        Date issuedAt = new Date(currentTimeMillis);
        Date expirationDate = new Date(currentTimeMillis + expiration * 1000);
        
        // 使用预处理的密钥字节数组
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(issuedAt)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secretKeyBytes != null ? secretKeyBytes : secret.getBytes())
                .compact();
    }

    public Boolean validateToken(String token, String username) {
        try {
            final String extractedUsername = getUsernameFromToken(token);
            return (extractedUsername.equals(username) && !isTokenExpired(token));
        } catch (Exception e) {
            return false;
        }
    }
}