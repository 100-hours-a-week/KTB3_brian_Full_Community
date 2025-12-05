package com.community.domain.auth.controller;

import com.community.domain.auth.dto.LoginResult;
import com.community.domain.auth.utils.AuthResponseMaker;
import com.community.domain.auth.service.AuthService;
import com.community.domain.auth.dto.response.LoginResponse;
import com.community.domain.auth.dto.request.LoginRequest;
import com.community.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController implements AuthApiSpec {

    private final AuthService authService;
    private final AuthResponseMaker authResponseMaker;

    @Override
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@RequestBody @Valid LoginRequest req) {
        LoginResult result = authService.login(req);

        return authResponseMaker.makeLoginResponse(result);
    }

    @Override
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<LoginResponse>> refresh(
            @CookieValue(value = "refreshToken") String refreshToken
    ) {
        LoginResult result = authService.refresh(refreshToken);

        return authResponseMaker.makeRefreshResponse(result);
    }
}
