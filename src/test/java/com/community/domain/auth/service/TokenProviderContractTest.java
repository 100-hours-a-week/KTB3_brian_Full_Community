package com.community.domain.auth.service;

import com.community.domain.auth.TokenType;
import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.auth.dto.TokenPayload;
import com.community.domain.auth.dto.TokenResult;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

/**
 * Contract tests verifying the expected behavior of any {@link TokenProvider} implementation.
 */
@ExtendWith(MockitoExtension.class)
public abstract class TokenProviderContractTest {

    private static final long ACCESS_EXPIRES_IN = 60L;
    private static final long REFRESH_EXPIRES_IN = 120L;

    protected TokenProvider tokenProvider;

    @Mock
    protected HttpServletRequest request;

    @BeforeEach
    void initProvider() {
        tokenProvider = createTokenProvider(ACCESS_EXPIRES_IN, REFRESH_EXPIRES_IN);
    }

    protected abstract TokenProvider createTokenProvider(long accessExpirationSeconds, long refreshExpirationSeconds);

    @Test
    @DisplayName("createToken 으로 생성한 토큰은 parseToken 으로 복호화할 수 있다.")
    void createToken_and_parseToken() {
        TokenResult result = tokenProvider.createToken(Map.of("sub", 42L), TokenType.ACCESS);

        assertThat(result.token()).isNotBlank();
        assertThat(result.expiresAt()).isAfter(Instant.now());

        TokenPayload payload = tokenProvider.parseToken(result.token(), TokenType.ACCESS);
        assertThat(payload.userId()).isEqualTo(42L);
        assertThat(payload.type()).isEqualTo(TokenType.ACCESS.name());
        assertThat(payload.expiresAt()).isAfter(Instant.now());
    }

    @Test
    @DisplayName("토큰 타입이 다르면 INVALID_TOKEN 예외를 던진다.")
    void parseToken_throws_when_typeMismatch() {
        String refreshToken = tokenProvider.createToken(Map.of("sub", 100L), TokenType.REFRESH).token();

        CustomException ex = assertThrows(CustomException.class,
                () -> tokenProvider.parseToken(refreshToken, TokenType.ACCESS));

        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.INVALID_TOKEN);
    }

    @Test
    @DisplayName("만료된 토큰은 EXPIRED_TOKEN 예외를 던진다.")
    void parseToken_throws_when_expired() {
        TokenProvider expiredProvider = createTokenProvider(-1L, -1L);
        String expiredToken = expiredProvider.createToken(Map.of("sub", 1L), TokenType.ACCESS).token();

        CustomException ex = assertThrows(CustomException.class,
                () -> expiredProvider.parseToken(expiredToken, TokenType.ACCESS));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.EXPIRED_TOKEN);
    }

    @Test
    @DisplayName("Authorization 헤더에서 Bearer 토큰을 추출한다.")
    void resolveToken() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Bearer sample.token.value");

        String token = tokenProvider.resolveToken(request);

        assertThat(token).isEqualTo("sample.token.value");
    }

    @Test
    @DisplayName("Authorization 헤더가 없거나 잘못된 경우 UNAUTHORIZED_USER 예외를 던진다.")
    void resolveToken_throws_when_header_invalid() {
        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn(null);

        CustomException ex = assertThrows(CustomException.class,
                () -> tokenProvider.resolveToken(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);

        when(request.getHeader(HttpHeaders.AUTHORIZATION)).thenReturn("Basic abc");
        ex = assertThrows(CustomException.class, () -> tokenProvider.resolveToken(request));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.UNAUTHORIZED_USER);
    }

    @Test
    @DisplayName("getAuthentication 은 토큰의 사용자 정보를 담은 Authentication 을 반환한다.")
    void getAuthentication_and_authenticatedUser() {
        String accessToken = tokenProvider.createToken(Map.of("sub", 55L), TokenType.ACCESS).token();

        Authentication authentication = tokenProvider.getAuthentication(accessToken);

        assertThat(authentication.isAuthenticated()).isTrue();
        assertThat(authentication.getPrincipal()).isInstanceOf(AuthenticatedUser.class);
        assertThat(((AuthenticatedUser) authentication.getPrincipal()).userId()).isEqualTo(55L);
    }
}
