package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    @Value("${jwt.secret}")
    private String jwtSecretString;

    private Key getSigningKey() {
        byte[] keyBytes = this.jwtSecretString.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }


    public String generateToken(Integer userId, String username){
        Map<String, Object> claims = new HashMap<>();
        claims.put("nickname", username);

        //private final Key secretKey = Jwts.SIG.HS256.key().build();
        long jwtExpirationTime = AppConstants.JWT_EXPIRATION_TIME_SECONDS * 1000L;
        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(this.getSigningKey())
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser().verifyWith((SecretKey)this.getSigningKey()).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getUserIdFromToken(String token){
        Claims claims = Jwts.parser().verifyWith((SecretKey) this.getSigningKey()).build().parseSignedClaims(token).getPayload();
        return Integer.parseInt(claims.getSubject());
    }

    public void applyTokenToCookie(User user, HttpServletResponse response){
        String newToken = this.generateToken(user.getId(), user.getNickname());
        ResponseCookie cookie = this.buildCookie(newToken);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    public ResponseCookie buildCookie (String token){
        return ResponseCookie.from("auth_token", token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .sameSite("Lax")
                .maxAge(86400)
                .build();
    }
    public void removeTokenFromCookie(HttpServletResponse response){
        response.addHeader(HttpHeaders.SET_COOKIE, "auth_token=; Path=/; Max-Age=0");
    }

}
