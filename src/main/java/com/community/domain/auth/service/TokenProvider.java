package com.community.domain.auth.service;

import com.community.domain.auth.TokenType;
import com.community.domain.auth.dto.TokenPayload;
import com.community.domain.auth.dto.TokenResult;

import java.util.Map;

public interface TokenProvider {

    TokenPayload parseToken(String accessToken, TokenType tokenType);

    TokenResult createToken(Map<String, Object> claims, TokenType tokenType);
}
