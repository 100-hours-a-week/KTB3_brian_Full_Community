package com.community.domain.user.controller;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.user.dto.request.PasswordUpdateRequest;
import com.community.domain.user.dto.request.SignInRequest;
import com.community.domain.user.dto.request.UpdateRequest;
import com.community.domain.user.dto.response.SignInAvailableResponse;
import com.community.domain.user.dto.response.SignInResponse;
import com.community.domain.user.dto.response.UserResponse;
import com.community.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "User", description = "회원 관리 API")
public interface UserApiSpec {

    @Operation(summary = "회원가입", description = "회원 가입을 요청합니다.")
    ResponseEntity<ApiResponse<SignInResponse>> signIn(SignInRequest req);

    @Operation(summary = "회원가입 이메일 검증", description = "이메일 사용 가능 여부를 확인합니다.")
    ResponseEntity<ApiResponse<SignInAvailableResponse>> checkEmailAvailability(String email);

    @Operation(summary = "회원가입 닉네임 검증", description = "닉네임 사용 가능 여부를 확인합니다.")
    ResponseEntity<ApiResponse<SignInAvailableResponse>> checkNicknameAvailability(String nickname);

    @Operation(summary = "회원 정보 조회", description = "본인 회원 정보를 조회합니다.")
    ResponseEntity<ApiResponse<UserResponse>> getMyProfile(AuthenticatedUser authenticatedUser);

    @Operation(summary = "회원 정보 수정", description = "본인 회원 정보를 수정합니다.")
    ResponseEntity<ApiResponse<Void>> updateProfile(AuthenticatedUser authenticatedUser,
                                                    UpdateRequest req);

    @Operation(summary = "회원 비밀번호 수정", description = "본인 비밀번호를 수정합니다.")
    ResponseEntity<ApiResponse<Void>> updatePassword(AuthenticatedUser authenticatedUser,
                                                     PasswordUpdateRequest req);

    @Operation(summary = "회원 탈퇴", description = "회원 탈퇴를 진행합니다. 관련된 정보를 삭제합니다.")
    ResponseEntity<ApiResponse<Void>> deleteUser(AuthenticatedUser authenticatedUser);
}
