package com.community.domain.user.controller;

import com.community.domain.auth.AuthUserArgumentResolver;
import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.user.dto.response.SignInAvailableResponse;
import com.community.domain.user.dto.response.SignInResponse;
import com.community.domain.user.dto.response.UserResponse;
import com.community.domain.user.service.UserService;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import com.community.global.response.SuccessMessage;
import com.community.global.validation.MessageConstants;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = UserController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class},
        excludeFilters = {
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = {OncePerRequestFilter.class})
        })
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(UserControllerTest.MvcTestConfig.class)
class UserControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    UserService userService;

    @MockitoBean
    JpaMetamodelMappingContext jpaMetamodelMappingContext;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("회원가입에 성공하면 201 응답과 유저의 id 를 JSON 으로 받는다.")
    void signIn() throws Exception {
        when(userService.signIn(any())).thenReturn(new SignInResponse(1L));

        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "Password1!")
                        .param("nickname", "nickname"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value(SuccessMessage.SIGN_UP_SUCCESS))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(userService).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 이메일이 비어있으면 400 응답을 받는다.")
    void signIn_throws_when_email_null() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("password", "Password1!")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 이메일이 공백 값이면 400 응답을 받는다.")
    void signIn_throws_when_email_empty() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "")
                        .param("password", "Password1!")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 이메일 형식이 아니면 400 응답을 받는다.")
    void signIn_throws_when_email_invalid_format() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "invalid-email")
                        .param("password", "Password1!")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호가 비어있으면 400 응답을 받는다.")
    void signIn_throws_when_password_null() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호 형식에 부합하지 않으면 400 응답을 받는다.")
    void signIn_throws_when_password_format_invalid() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "abcdedfhij")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호 길이가 너무 짧으면 400 응답을 받는다.")
    void signIn_throws_when_password_short() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "Pass1!")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 비밀번호 길이가 너무 길면 400 응답을 받는다.")
    void signIn_throws_when_password_long() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "Abcdedfhijk1234567890!")
                        .param("nickname", "nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 닉네임이 비어 있으면 400 응답을 받는다.")
    void signIn_throws_when_nickname_null() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "Password1!"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 닉네임이 공백 문자를 포함하면 400 응답을 받는다.")
    void signIn_throws_when_nickname_whitespace() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "Password1!")
                        .param("nickname", " a "))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("회원 가입 시 닉네임 길이가 10자를 초과하면 400 응답을 받는다.")
    void signIn_throws_when_nickname_too_long() throws Exception {
        mockMvc.perform(multipart("/users")
                        .contentType(MediaType.MULTIPART_FORM_DATA)
                        .param("email", "email@email.com")
                        .param("password", "Password1!")
                        .param("nickname", "nickname-too-long"))
                .andExpect(jsonPath("$.message").value(containsString(MessageConstants.NICKNAME_SIZE_INVALID)))
                .andExpect(status().isBadRequest());

        verify(userService, never()).signIn(any());
    }

    @Test
    @DisplayName("이메일 중복 확인 성공 시 200 과 사용 가능 여부를 반환한다.")
    void checkEmailAvailability() throws Exception {
        when(userService.checkEmailAvailability("available@email.com"))
                .thenReturn(new SignInAvailableResponse(true));

        mockMvc.perform(get("/users/availability/email")
                        .param("email", "available@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessage.SIGN_UP_INFO_AVAILABLE))
                .andExpect(jsonPath("$.data.isSignInInformationAvailable").value(true));

        verify(userService).checkEmailAvailability("available@email.com");
    }

    @Test
    @DisplayName("이메일 중복 확인 시 이메일이 비어 있으면 400 을 반환하고 서비스가 호출되지 않는다.")
    void checkEmailAvailability_throws_when_email_empty() throws Exception {
        mockMvc.perform(get("/users/availability/email"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).checkEmailAvailability(any());
    }

    @Test
    @DisplayName("이메일 중복 확인 시 이메일이 중복이면 409 를 반환한다. ")
    void checkEmailAvailability_throws_when_email_duplicate() throws Exception {
        when(userService.checkEmailAvailability("dup@email.com"))
                .thenThrow(new CustomException(ErrorCode.DUPLICATED_EMAIL));

        mockMvc.perform(get("/users/availability/email")
                        .param("email", "dup@email.com"))
                .andExpect(status().isConflict());
    }


    @Test
    @DisplayName("닉네임 중복 확인 성공 시 200 과 사용 가능 여부를 반환한다.")
    void checkNicknameAvailability() throws Exception {
        when(userService.checkNicknameAvailability("nickname"))
                .thenReturn(new SignInAvailableResponse(true));

        mockMvc.perform(get("/users/availability/nickname")
                        .param("nickname", "nickname"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessage.SIGN_UP_INFO_AVAILABLE))
                .andExpect(jsonPath("$.data.isSignInInformationAvailable").value(true));

        verify(userService).checkNicknameAvailability("nickname");
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 닉네임이 비어 있으면 400 을 반환하고 서비스가 호출되지 않는다.")
    void checkNicknameAvailability_throws_when_nickname_empty() throws Exception {
        mockMvc.perform(get("/users/availability/nickname"))
                .andExpect(status().isBadRequest());

        verify(userService, never()).checkNicknameAvailability(any());
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 닉네임이 중복이면 409 를 반환한다. ")
    void checkNicknameAvailability_throws_when_nickname_duplicate() throws Exception {
        when(userService.checkNicknameAvailability("dupName"))
                .thenThrow(new CustomException(ErrorCode.DUPLICATED_NICKNAME));

        mockMvc.perform(get("/users/availability/nickname")
                        .param("nickname", "dupName"))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("프로필을 조회하면 200 성공과 유저의 프로필 정보를 반환받는다.")
    void getMyProfile() throws Exception {
        //given
        authenticate(1L);

        when(userService.getUserProfile(1L)).thenReturn(new UserResponse(1L, "e", "n", "i"));

        //when + then
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessage.PROFILE_FETCHED))
                .andExpect(jsonPath("$.data.id").value(1L));

        verify(userService).getUserProfile(1L);
    }

    @Test
    @DisplayName("프로필을 수정하면 200 과 성공 메시지를 반환하고 서비스가 호출된다.")
    void updateProfile() throws Exception {
        authenticate(10L);

        MockMultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "content".getBytes());

        mockMvc.perform(multipart("/users/me")
                        .file(file)
                        .param("nickname", "newName")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessage.PROFILE_UPDATED));

        verify(userService).updateProfile(eq(10L), any());
    }

    @Test
    @DisplayName("프로필 수정 시 닉네임과 파일 모두 비어 있으면 400을 반환한다.")
    void updateProfile_validation_error() throws Exception {
        authenticate(10L);

        mockMvc.perform(multipart("/users/me")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString(MessageConstants.PROFILE_UPDATE_REQUIRED)));

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("프로필 수정 시 닉네임 길이가 10자를 넘으면 400을 반환한다.")
    void updateProfile_nickname_too_long() throws Exception {
        authenticate(10L);

        mockMvc.perform(multipart("/users/me")
                        .param("nickname", "nickname-too-long")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("프로필 수정 시 닉네임이 공백을 포함하면 400을 반환한다.")
    void updateProfile_nickname_whitespace() throws Exception {
        authenticate(10L);

        mockMvc.perform(multipart("/users/me")
                        .param("nickname", "   ")
                        .with(request -> {
                            request.setMethod("PATCH");
                            return request;
                        }))
                .andExpect(status().isBadRequest());

        verify(userService, never()).updateProfile(any(), any());
    }

    @Test
    @DisplayName("비밀번호 수정 요청은 200 과 성공 메시지를 반환하고 서비스가 호출된다.")
    void updatePassword() throws Exception {
        authenticate(5L);

        mockMvc.perform(patch("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "Password1!"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value(SuccessMessage.PASSWORD_UPDATED));

        verify(userService).changePassword(eq(5L), any());
    }

    @Test
    @DisplayName("비밀번호 수정 시 비밀번호가 비어 있으면 400을 반환한다.")
    void updatePassword_validation_error() throws Exception {
        authenticate(5L);

        mockMvc.perform(patch("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": ""
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("비밀번호 수정 시 길이가 8자 미만이면 400을 반환한다.")
    void updatePassword_short_password() throws Exception {
        authenticate(5L);

        mockMvc.perform(patch("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "Pass1!"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("비밀번호 수정 시 길이가 20자를 초과하면 400을 반환한다.")
    void updatePassword_too_long() throws Exception {
        authenticate(5L);

        mockMvc.perform(patch("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "Password1234567890!!!!"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("비밀번호 수정 시 대소문자, 숫자, 특수문자를 모두 포함하지 않으면 400을 반환한다.")
    void updatePassword_invalid_format() throws Exception {
        authenticate(5L);

        mockMvc.perform(patch("/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "password": "password1234!"
                                }
                                """))
                .andExpect(status().isBadRequest());

        verify(userService, never()).changePassword(any(), any());
    }

    @Test
    @DisplayName("유저 삭제 시 204 응답을 반환하고 비어있는 바디를 반환한다.")
    void deleteUser() throws Exception {
        authenticate(7L);

        mockMvc.perform(delete("/users/me"))
                .andExpect(status().isNoContent())
                .andExpect(content().bytes(new byte[0]));

        verify(userService).deleteUser(7L);
    }

    private void authenticate(Long userId) {
        TestingAuthenticationToken auth =
                new TestingAuthenticationToken(new AuthenticatedUser(userId), null);
        auth.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @TestConfiguration
    @RequiredArgsConstructor
    static class MvcTestConfig implements WebMvcConfigurer {

        private final AuthUserArgumentResolver authUserArgumentResolver;

        @Override
        public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
            resolvers.add(authUserArgumentResolver);
        }
    }
}
