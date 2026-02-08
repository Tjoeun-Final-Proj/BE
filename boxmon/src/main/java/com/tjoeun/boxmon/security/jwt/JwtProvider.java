package com.tjoeun.boxmon.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;


// 토근 생성, 검증, 사용자 식별 정보 추출 담당
@Component
public class JwtProvider {
    private final String SECRET_KEY = System.getenv("JWT_SECRET"); //JWT비밀키 ... 환경변수로 지정하여 사용
    private final long ACCESS_TOKEN_EXPIRE_TIME = 1000L * 60 * 15; //Access Token 만료시간 (15분)
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 14; // Refresh Token 만료시간 (14일)


    // 공통 키 생성
    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(
                SECRET_KEY.getBytes(StandardCharsets.UTF_8)
        );
    }

    //Access Token 생성
    public String createAccessToken(Long userId){
        return Jwts.builder()
                .setSubject(String.valueOf(userId))//userId 데이터 전달
                .claim("type", "ACCESS")
                .setIssuedAt(new Date())//token 발급시간
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXPIRE_TIME))//token 만료시간
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)//해시함수 비밀키 ... RS256도 있음
                .compact();//JWT문자열
    }

    //Refresh Token 생성
    public String createRefreshToken(Long userId){
        return Jwts.builder()
                .setSubject(String.valueOf(userId))//userId 데이터 전달
                .claim("type", "REFRESH")
                .setIssuedAt(new Date())//token 발급시간
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME))//token 만료시간
                .signWith(getSigningKey(),SignatureAlgorithm.HS256)//해시함수 비밀키 ... RS256도 있음
                .compact();
    }

    // 토큰 타입 확인
    public String getTokenType(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody();

        return claims.get("type", String.class);
    }


    //userId 추출 (토큰파싱)
    //서명, 만료 검증
    public Long getUserIdFromToken(String token){
        Claims claims = Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getBody();

        return Long.valueOf(claims.getSubject());
    }

    //token 유효성 검증 ... 이라는데 이건 뭔지 잘 모르겠음. jwt 내에서 예외를 처리하는듯?
    public boolean validateToken(String token){
        try {
            Jwts.parser()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


}
