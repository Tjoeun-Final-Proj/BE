package com.tjoeun.boxmon.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;

    public JwtFilter(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        System.out.println("🔥 JwtFilter 실행됨");
        System.out.println("Authorization = " + request.getHeader("Authorization"));

        // Authorization 헤더에서 토큰 꺼내기
        String authHeader = request.getHeader("Authorization");

        // 토큰 없으면 그냥 다음 필터로
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // "Bearer " 제거
        String token = authHeader.substring(7);

        // 토큰 검증
        if (!jwtProvider.validateToken(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 토큰에서 userId 추출
        Long userId = jwtProvider.getUserIdFromToken(token);

        // 인증 객체 생성 (권한은 아직 없음)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        userId,              // principal
                        null,                // credentials
                        Collections.emptyList() // authorities
                );

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);


        // 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}
