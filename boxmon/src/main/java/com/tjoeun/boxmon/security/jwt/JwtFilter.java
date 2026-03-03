package com.tjoeun.boxmon.security.jwt;

import com.tjoeun.boxmon.exception.AccountBlockedException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@Slf4j
@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final JwtService jwtService;

    public JwtFilter(JwtProvider jwtProvider, JwtService jwtService) {
        this.jwtProvider = jwtProvider;
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

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
            log.warn("token 검증 실패");
            filterChain.doFilter(request, response);
            return;
        }

        // Refresh Token으로 API 접근 차단
        String tokenType = jwtProvider.getTokenType(token);
        if ("REFRESH".equals(tokenType)) {
            log.warn("refresh token로 접근하였습니다");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        // 토큰에서 userId 추출 + account_status 검증
        Long accountId = jwtProvider.getUserIdFromToken(token);
        if(!jwtProvider.checkAdmin(token)) {
            if (!jwtService.statusCheck(accountId)) {
                throw new AccountBlockedException("차단된 계정입니다.");
            }
        }
        // 인증 객체 생성 (권한은 아직 없음)
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        accountId,              // principal
                        null,                // credentials
                        Collections.emptyList() // authorities
                );

        // SecurityContext에 인증 정보 저장
        SecurityContextHolder.getContext().setAuthentication(authentication);


        // 다음 필터로 이동
        filterChain.doFilter(request, response);
    }
}
