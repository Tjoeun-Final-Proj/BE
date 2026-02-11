package com.tjoeun.boxmon.security.jwt;

import com.tjoeun.boxmon.exception.InvalidTokenException;
import com.tjoeun.boxmon.exception.TokenTypeMismatchException;
import com.tjoeun.boxmon.feature.user.dto.TokenRefreshRequest;
import com.tjoeun.boxmon.feature.user.dto.TokenRefreshResponse;
import org.springframework.stereotype.Service;

@Service
public class RefreshTokenService {

    private final JwtProvider jwtProvider;

    public RefreshTokenService(JwtProvider jwtProvider) {
        this.jwtProvider = jwtProvider;
    }

    // Access Token 갱신
    public TokenRefreshResponse refreshToken(TokenRefreshRequest request) {
        String refreshTokenValue = request.getRefreshToken();

        // Refresh Token 유효성 검증 (서명, 만료 확인)
        if (!jwtProvider.validateToken(refreshTokenValue)) {
            throw new InvalidTokenException("유효하지 않은 Refresh Token");
        }

        // Refresh Token 타입 확인
        String tokenType = jwtProvider.getTokenType(refreshTokenValue);
        if (!"REFRESH".equals(tokenType)) {
            throw new TokenTypeMismatchException("Refresh Token이 아닙니다");
        }

        // Refresh Token에서 userId 추출
        Long userId = jwtProvider.getUserIdFromToken(refreshTokenValue);

        // 새로운 Access Token 생성
        String newAccessToken = jwtProvider.createAccessToken(userId);

        // 새로운 Refresh Token 생성
        String newRefreshToken = jwtProvider.createRefreshToken(userId);

        return new TokenRefreshResponse(newAccessToken, newRefreshToken);
    }
}
