package com.community.domain.user.controller;

import com.community.domain.auth.annotation.Auth;
import com.community.domain.auth.annotation.AuthUser;
import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.common.util.UriUtil;
import com.community.domain.user.dto.request.PasswordUpdateRequest;
import com.community.domain.user.dto.request.SignInRequest;
import com.community.domain.user.dto.request.UpdateRequest;
import com.community.domain.user.dto.response.SignInAvailableResponse;
import com.community.domain.user.dto.response.SignInResponse;
import com.community.domain.user.dto.response.UserResponse;
import com.community.domain.user.service.UserService;
import com.community.global.response.ApiResponse;
import com.community.global.response.SuccessMessage;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController implements UserApiSpec {

    private final UserService userService;

    @Override
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<SignInResponse>> signIn(@ModelAttribute @Valid SignInRequest req) {

        SignInResponse res = userService.signIn(req);

        return ResponseEntity
                .created(UriUtil.makeLocationFromCurrent(res.getId()))
                .body(ApiResponse.success(SuccessMessage.SIGN_UP_SUCCESS, res));
    }

    @Override
    @GetMapping("/availability")
    public ResponseEntity<ApiResponse<SignInAvailableResponse>> checkAvailableSignInInfo(@RequestParam @Nullable String email,
                                                                                         @RequestParam @Nullable String nickname) {
        SignInAvailableResponse res = userService.checkAvailableSignInInfo(email, nickname);
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.SIGN_UP_INFO_AVAILABLE, res));
    }

    @Override
    @Auth
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getMyProfile(@AuthUser AuthenticatedUser authenticatedUser) {
        UserResponse profile = userService.getUserProfile(authenticatedUser.userId());
        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.PROFILE_FETCHED, profile));
    }

    @Override
    @Auth
    @PatchMapping(value = "/me", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthUser AuthenticatedUser authenticatedUser,
                                                           @ModelAttribute @Valid UpdateRequest req) {
        userService.updateProfile(authenticatedUser.userId(), req);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.PROFILE_UPDATED));
    }

    @Override
    @Auth
    @PatchMapping("/me/password")
    public ResponseEntity<ApiResponse<Void>> updatePassword(@AuthUser AuthenticatedUser authenticatedUser,
                                                            @RequestBody @Valid PasswordUpdateRequest req) {
        userService.changePassword(authenticatedUser.userId(), req);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success(SuccessMessage.PASSWORD_UPDATED));
    }

    @Override
    @Auth
    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@AuthUser AuthenticatedUser authenticatedUser) {
        userService.deleteUser(authenticatedUser.userId());

        return ResponseEntity
                .noContent()
                .build();
    }
}
