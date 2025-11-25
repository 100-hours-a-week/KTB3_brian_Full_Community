package com.community.global.security;

import com.community.domain.auth.service.TokenProvider;
import com.community.global.exception.CustomException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final CustomAuthenticationExceptionResolver customAuthenticationExceptionResolver;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = tokenProvider.resolveToken(request);
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            filterChain.doFilter(request, response);
        } catch (CustomException ex) {
            throw customAuthenticationExceptionResolver.resolve(ex);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        boolean isPermitAllPath = Arrays.stream(SecurityConfig.PERMIT_ALL_MATCHERS)
                .anyMatch(pattern -> pattern.matches(request));
        if (isPermitAllPath) {
            return true;
        }

        String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
        return authorization == null || !authorization.startsWith("Bearer ");
    }
}
