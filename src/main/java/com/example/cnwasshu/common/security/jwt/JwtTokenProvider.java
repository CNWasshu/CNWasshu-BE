package com.example.cnwasshu.common.security.jwt;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.cnwasshu.common.exception.BusinessException;
import com.example.cnwasshu.common.exception.ErrorCode;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private static final String CLAIM_TOKEN_TYPE = "tokenType";
    private static final String TOKEN_TYPE_ACCESS = "ACCESS";
    private static final String TOKEN_TYPE_REFRESH = "REFRESH";

    private final JwtProperties jwtProperties;

    private SecretKey secretKey() {
        return Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(Long userId) {
        return generateToken(userId, TOKEN_TYPE_ACCESS, jwtProperties.accessTokenValiditySeconds());
    }

    public String generateRefreshToken(Long userId) {
        return generateToken(userId, TOKEN_TYPE_REFRESH, jwtProperties.refreshTokenValiditySeconds());
    }

    public long getAccessTokenValiditySeconds() {
        return jwtProperties.accessTokenValiditySeconds();
    }

    public long getRefreshTokenValiditySeconds() {
        return jwtProperties.refreshTokenValiditySeconds();
    }

    private String generateToken(Long userId, String tokenType, long validitySeconds) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim(CLAIM_TOKEN_TYPE, tokenType)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(validitySeconds)))
                .signWith(secretKey())
                .compact();
    }

    public Long getUserId(String token) {
        return Long.valueOf(parseClaims(token).getSubject());
    }

    public void validateAccessToken(String token) {
        validateTokenType(token, TOKEN_TYPE_ACCESS);
    }

    public void validateRefreshToken(String token) {
        validateTokenType(token, TOKEN_TYPE_REFRESH);
    }

    private void validateTokenType(String token, String expectedType) {
        String actualType = (String) parseClaims(token).get(CLAIM_TOKEN_TYPE);
        if (!expectedType.equals(actualType)) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }

    private io.jsonwebtoken.Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(secretKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.EXPIRED_TOKEN);
        } catch (JwtException | IllegalArgumentException e) {
            throw new BusinessException(ErrorCode.INVALID_TOKEN);
        }
    }
}
