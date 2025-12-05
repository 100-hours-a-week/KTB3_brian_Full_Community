package com.community.domain.auth.service;

import com.community.domain.auth.TokenType;
import com.community.domain.auth.dto.TokenPayload;
import com.community.domain.auth.dto.TokenResult;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;

import java.util.Map;

public interface TokenProvider {

    TokenPayload parseToken(String accessToken, TokenType tokenType);

    TokenResult createToken(Map<String, Object> claims, TokenType tokenType);

    String resolveToken(HttpServletRequest request);

    Authentication getAuthentication(String token);
}
