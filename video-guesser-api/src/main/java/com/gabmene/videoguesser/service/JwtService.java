package com.gabmene.videoguesser.service;

import com.gabmene.videoguesser.constants.AppConstants;
import com.gabmene.videoguesser.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JwtService {
    private final Key secretKey = Jwts.SIG.HS256.key().build();
    private final long jwtExpirationTime = AppConstants.JWT_EXPIRATION_TIME_SECONDS * 1000L;

    public String generateToken(Integer userId, String username){
        Map<String, Object> claims = new HashMap<>();
        claims.put("nickname", username);

        return Jwts.builder()
                .claims(claims)
                .subject(String.valueOf(userId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationTime))
                .signWith(secretKey)
                .compact();
    }

    public boolean validateToken(String token){
        try{
            Jwts.parser().verifyWith((SecretKey) secretKey).build().parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Integer getUserIdFromToken(String token){
        Claims claims = Jwts.parser().verifyWith((SecretKey) secretKey).build().parseSignedClaims(token).getBody();
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

}
