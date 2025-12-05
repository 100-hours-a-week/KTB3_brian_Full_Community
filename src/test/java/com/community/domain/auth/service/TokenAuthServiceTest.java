package com.community.domain.auth.service;

import com.community.domain.auth.TokenType;
import com.community.domain.auth.dto.LoginResult;
import com.community.domain.auth.dto.TokenPayload;
import com.community.domain.auth.dto.TokenResult;
import com.community.domain.auth.dto.request.LoginRequest;
import com.community.domain.auth.model.RefreshToken;
import com.community.domain.auth.repository.RefreshTokenRepository;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static com.community.helper.UserMaker.*;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenAuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private TokenProvider tokenProvider;

    @InjectMocks
    private TokenAuthService tokenAuthService;

    @Test
    @DisplayName("정상 로그인 시 어세스/리프레시 토큰을 발급하고 저장한다.")
    void login_issues_tokens_and_persists_refreshToken() {
        //given
        User user = getIdentifyingUser(1);
        LoginRequest request = loginRequest("test1@test.com", "Password!1");

        TokenResult accessToken = new TokenResult("access-token", Instant.now().plusSeconds(3600));
        TokenResult refreshToken = new TokenResult("refresh-token", Instant.now().plusSeconds(7200));

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(tokenProvider.createToken(anyMap(), eq(TokenType.ACCESS))).thenReturn(accessToken);
        when(tokenProvider.createToken(anyMap(), eq(TokenType.REFRESH))).thenReturn(refreshToken);

        //when
        LoginResult result = tokenAuthService.login(request);

        //then
        assertThat(result.tokenResponse().getAccessToken()).isEqualTo("access-token");
        assertThat(result.tokenResponse().getType()).isEqualTo("Bearer");
        assertThat(result.tokenResponse().getExpiresIn()).isPositive();
        assertThat(result.refreshToken()).isEqualTo("refresh-token");
        assertThat(result.refreshTokenExpiresInSeconds()).isPositive();

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("refresh-token");
        assertThat(captor.getValue().getUserId()).isEqualTo(1L);
        assertThat(captor.getValue().getExpiresAt()).isEqualTo(refreshToken.expiresAt());
    }

    @Test
    @DisplayName("로그인 시 이메일로 사용자를 찾지 못하면 LOGIN_FAILED 예외를 던진다.")
    void login_throws_login_failed_when_findByEmail_empty() {
        //given
        LoginRequest request = loginRequest("missing@test.com", "password");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        //when
        CustomException exception = assertThrows(CustomException.class, () -> tokenAuthService.login(request));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않으면 LOGIN_FAILED 예외를 던진다.")
    void login_throws_login_failed_when_password_not_match() {
        //given
        LoginRequest request = loginRequest("test1@email.com", "pwDifferent!1");
        User user = getIdentifyingUser(1);
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        //when + then
        CustomException exception = assertThrows(CustomException.class, () -> tokenAuthService.login(request));
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.LOGIN_FAILED);
    }

    @Test
    @DisplayName("유효한 리프레시 토큰으로 갱신 요청 시 기존 토큰을 삭제하고 새 토큰을 발급한다.")
    void refresh_deletes_existing_and_issues_newTokens() {
        //given
        String refreshTokenValue = "refresh-token";
        TokenPayload payload = new TokenPayload(7L, Instant.now().plusSeconds(7200), TokenType.REFRESH.name());
        TokenResult newAccessToken = new TokenResult("new-access", Instant.now().plusSeconds(3600));
        TokenResult newRefreshToken = new TokenResult("new-refresh", Instant.now().plusSeconds(10800));

        when(tokenProvider.parseToken(refreshTokenValue, TokenType.REFRESH)).thenReturn(payload);
        when(tokenProvider.createToken(anyMap(), eq(TokenType.ACCESS))).thenReturn(newAccessToken);
        when(tokenProvider.createToken(anyMap(), eq(TokenType.REFRESH))).thenReturn(newRefreshToken);

        //when
        LoginResult result = tokenAuthService.refresh(refreshTokenValue);

        //then
        assertThat(result.tokenResponse().getAccessToken()).isEqualTo("new-access");
        assertThat(result.tokenResponse().getType()).isEqualTo("Bearer");
        assertThat(result.refreshToken()).isEqualTo("new-refresh");
        assertThat(result.refreshTokenExpiresInSeconds()).isPositive();

        verify(refreshTokenRepository).delete(refreshTokenValue);
        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(refreshTokenRepository).save(captor.capture());
        assertThat(captor.getValue().getToken()).isEqualTo("new-refresh");
        assertThat(captor.getValue().getUserId()).isEqualTo(7L);
    }

    @Test
    @DisplayName("리프레시 토큰이 비어 있으면 REFRESH_TOKEN_MISMATCH 예외를 던진다.")
    void refresh_throws_token_mismatch_when_token_blank() {
        //when
        CustomException exception = assertThrows(CustomException.class, () -> tokenAuthService.refresh(" "));

        //then
        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.REFRESH_TOKEN_MISMATCH);
        verifyNoInteractions(tokenProvider, refreshTokenRepository);
    }

    private LoginRequest loginRequest(String email, String password) {
        LoginRequest request = new LoginRequest();
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
