package com.community.domain.auth.utils;

import com.community.domain.auth.dto.LoginResult;
import com.community.domain.auth.dto.response.LoginResponse;
import com.community.global.response.ApiResponse;
import org.springframework.http.ResponseEntity;

public interface AuthResponseMaker {

    ResponseEntity<ApiResponse<LoginResponse>> makeLoginResponse(LoginResult result);

    ResponseEntity<ApiResponse<LoginResponse>> makeRefreshResponse(LoginResult result);
}
