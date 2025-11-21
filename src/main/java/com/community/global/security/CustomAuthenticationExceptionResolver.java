package com.community.global.security;

import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class CustomAuthenticationExceptionResolver {

    public AuthenticationException resolve(CustomException ex) {
        ErrorCode errorCode = ex.getErrorCode();

        return switch (errorCode) {
            case UNAUTHORIZED_USER -> new InsufficientAuthenticationException(errorCode.getMessage(), ex);
            case INVALID_TOKEN -> new BadCredentialsException(errorCode.getMessage(), ex);
            case EXPIRED_TOKEN -> new CredentialsExpiredException(errorCode.getMessage(), ex);
            default -> new AuthenticationServiceException(errorCode.getMessage(), ex);
        };
    }
}
