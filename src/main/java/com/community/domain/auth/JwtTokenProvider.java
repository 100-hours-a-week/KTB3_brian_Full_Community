package com.community.domain.auth;

import com.community.domain.auth.dto.TokenPayload;
import com.community.domain.auth.dto.TokenResult;
import com.community.domain.auth.service.TokenProvider;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider implements TokenProvider {

    @Value("${jwt.access-expiration-seconds}")
    private Long ACCESS_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.refresh-expiration-seconds}")
    private Long REFRESH_TOKEN_EXPIRATION_TIME;

    @Value("${jwt.secret}")
    private String JWT_SECRET;

    private static final String HMAC_ALGORITHM = "HmacSHA256";

    private static final Base64.Encoder BASE64_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;

    @Override
    public TokenPayload parseToken(String token, TokenType tokenType) {
        TokenPayload payload = validateAndGetTokenPayload(token);
        if (!tokenType.toString().equals(payload.type())) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return payload;
    }

    @Override
    public TokenResult createToken(Map<String, Object> claims, TokenType tokenType) {
        Long expiresIn = tokenType.equals(TokenType.ACCESS) ? ACCESS_TOKEN_EXPIRATION_TIME : REFRESH_TOKEN_EXPIRATION_TIME;
        return createToken(claims, expiresIn, tokenType);
    }

    private TokenPayload validateAndGetTokenPayload(String token) {
        String[] parts = splitToken(token);
        verifySignature(parts);
        Map<String, Object> payloadMap = parsePayload(parts[1]);
        verifyExpiry(payloadMap);
        return toTokenPayload(payloadMap);
    }

    private String[] splitToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
        return parts;
    }

    private void verifySignature(String[] parts) {
        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private Map<String, Object> parsePayload(String encodedPayload) {
        try {
            return objectMapper.readValue(
                    new String(BASE64_DECODER.decode(encodedPayload), StandardCharsets.UTF_8),
                    new TypeReference<>() {
                    }
            );
        } catch (JsonProcessingException | IllegalArgumentException ex) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }
    }

    private void verifyExpiry(Map<String, Object> payloadMap) {
        long expiresAt = ((Number) payloadMap.get("exp")).longValue();
        Instant expiryInstant = Instant.ofEpochSecond(expiresAt);
        if (Instant.now().isAfter(expiryInstant)) {
            throw new CustomException(ErrorCode.EXPIRED_TOKEN);
        }
    }

    private TokenPayload toTokenPayload(Map<String, Object> payloadMap) {
        Long userId = Long.valueOf(payloadMap.get("sub").toString());
        String type = payloadMap.get("type").toString();
        long expiresAt = ((Number) payloadMap.get("exp")).longValue();
        Instant expiryInstant = Instant.ofEpochSecond(expiresAt);

        return new TokenPayload(userId, expiryInstant, type);
    }

    private TokenResult createToken(Map<String, Object> claims, long expirationSeconds, TokenType type) {
        try {
            Instant now = Instant.now();
            Instant expiresAt = now.plusSeconds(expirationSeconds);

            Map<String, Object> header = Map.of(
                    "alg", "HS256",
                    "typ", "JWT"
            );

            Map<String, Object> payload = new HashMap<>(claims);
            payload.put("exp", expiresAt.getEpochSecond());
            payload.put("iat", now.getEpochSecond());
            payload.put("type", type);

            String encodedHeader = BASE64_ENCODER.encodeToString(objectMapper.writeValueAsBytes(header));
            String encodedPayload = BASE64_ENCODER.encodeToString(objectMapper.writeValueAsBytes(payload));
            String unsignedToken = encodedHeader + "." + encodedPayload;
            String signature = sign(unsignedToken);

            String token = unsignedToken + "." + signature;
            return new TokenResult(token, expiresAt);
        } catch (JsonProcessingException ex) {
            throw new CustomException(ErrorCode.TOKEN_GENERATION_ERROR);
        }
    }

    private String sign(String data) {
        try {
            Mac hmac = Mac.getInstance(HMAC_ALGORITHM);
            SecretKeySpec keySpec = new SecretKeySpec(JWT_SECRET.getBytes(StandardCharsets.UTF_8), HMAC_ALGORITHM);
            hmac.init(keySpec);
            byte[] signatureBytes = hmac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return BASE64_ENCODER.encodeToString(signatureBytes);
        } catch (NoSuchAlgorithmException | InvalidKeyException ex) {
            throw new IllegalStateException("Failed to sign JWT", ex);
        }
    }

    /**
     * 상수 시간에 서명을 비교하여 타이밍 공격 방지
     */
    private boolean constantTimeEquals(String expected, String actual) {
        if (expected.length() != actual.length()) {
            return false;
        }

        int result = 0;
        for (int i = 0; i < expected.length(); i++) {
            result |= expected.charAt(i) ^ actual.charAt(i);
        }
        return result == 0;
    }
}
