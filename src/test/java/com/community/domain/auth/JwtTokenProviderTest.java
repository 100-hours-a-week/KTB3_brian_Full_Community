package com.community.domain.auth;

import com.community.domain.auth.dto.TokenResult;
import com.community.domain.auth.service.JwtTokenProvider;
import com.community.domain.auth.service.TokenProvider;
import com.community.domain.auth.service.TokenProviderContractTest;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JwtTokenProviderTest extends TokenProviderContractTest {

    private static final String SECRET = "test-secret-key-1234567890";
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected TokenProvider createTokenProvider(long accessExpirationSeconds, long refreshExpirationSeconds) {
        JwtTokenProvider provider = new JwtTokenProvider(objectMapper);
        ReflectionTestUtils.setField(provider, "ACCESS_TOKEN_EXPIRATION_TIME", accessExpirationSeconds);
        ReflectionTestUtils.setField(provider, "REFRESH_TOKEN_EXPIRATION_TIME", refreshExpirationSeconds);
        ReflectionTestUtils.setField(provider, "JWT_SECRET", SECRET);
        return provider;
    }

    @Test
    @DisplayName("JWT 토큰의 파트가 3개가 아니면 INVALID_TOKEN 예외를 던진다.")
    void parseToken_throws_when_token_part_invalid() {
        CustomException customException = assertThrows(CustomException.class,
                () -> tokenProvider.parseToken("token.parts.is.invalid", TokenType.ACCESS));
        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("JWT 서명이 변조되면 INVALID_TOKEN 예외를 던진다.")
    void parseToken_throws_when_token_sign_invalid() throws IOException {
        TokenResult tokenResult = tokenProvider.createToken(Map.of("sub", 42L), TokenType.ACCESS);
        String[] parts = tokenResult.token().split("\\.");

        Base64.Decoder decoder = Base64.getUrlDecoder();
        Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

        Map<String, Object> payload = objectMapper.readValue(decoder.decode(parts[1]), new TypeReference<>() {
        });
        payload.put("sub", 41L);

        String tamperedPayload = encoder.encodeToString(objectMapper.writeValueAsBytes(payload));
        String tamperedToken = parts[0] + "." + tamperedPayload + "." + parts[2];

        CustomException customException = assertThrows(CustomException.class,
                () -> tokenProvider.parseToken(tamperedToken, TokenType.ACCESS));
        assertThat(customException.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }
}
