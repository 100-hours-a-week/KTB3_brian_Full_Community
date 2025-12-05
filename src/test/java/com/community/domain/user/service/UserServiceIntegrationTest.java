package com.community.domain.user.service;

import com.community.domain.file.service.FileStorageService;
import com.community.domain.user.dto.request.PasswordUpdateRequest;
import com.community.domain.user.dto.request.SignInRequest;
import com.community.domain.user.dto.request.UpdateRequest;
import com.community.domain.user.dto.response.SignInResponse;
import com.community.domain.user.dto.response.UserResponse;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private FileStorageService fileStorageService;

    @PersistenceContext
    private EntityManager em;

    @Value("${application.local.default_image_url}")
    private String defaultImageUrl;

    @Test
    @DisplayName("프로필 이미지를 첨부한 회원가입은 파일을 저장하고 사용자 정보를 영속화한다.")
    void signIn_persistsUser_withProfileImage() {
        //given
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image".getBytes());
        SignInRequest request = createSignInRequest(1, file);

        when(fileStorageService.save(any())).thenReturn("imageUrl");

        //when
        SignInResponse response = userService.signIn(request);

        //then
        assertNotNull(response.getId());
        User saved = userRepository.findById(response.getId()).orElseThrow();
        assertEquals(request.getEmail(), saved.getEmail());
        assertEquals(request.getNickname(), saved.getNickname());
        assertEquals("imageUrl", saved.getImageUrl());
        verify(fileStorageService).save(request.getFile());
    }

    @Test
    @DisplayName("프로필 이미지를 첨부하지 않은 회원가입은 기본 이미지를 사용한다.")
    void signIn_uses_defaultImage_when_file_missing() {
        //given
        SignInRequest request = createSignInRequest(1, null);

        //when
        SignInResponse response = userService.signIn(request);

        //then
        User saved = userRepository.findById(response.getId()).orElseThrow();
        assertEquals(defaultImageUrl, saved.getImageUrl());
        verify(fileStorageService, never()).save(any());
    }

    @Test
    @DisplayName("userId 로 회원 프로필을 조회할 수 있다.")
    void getUserProfile_and_response() {
        //given
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image".getBytes());
        SignInRequest request = createSignInRequest(1, file);
        when(fileStorageService.save(file)).thenReturn("imageUrl");

        Long userId = userService.signIn(request).getId();
        em.flush();
        em.clear();

        //when
        UserResponse response = userService.getUserProfile(userId);

        //then
        assertEquals(userId, response.getId());
        assertEquals(request.getNickname(), response.getNickname());
        assertEquals(request.getEmail(), response.getEmail());
    }

    @Test
    @DisplayName("회원의 닉네임과 이미지를 업데이트할 수 있다.")
    void updateProfile() {
        //given
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image".getBytes());
        SignInRequest signInRequest = createSignInRequest(1, file);
        when(fileStorageService.save(file)).thenReturn("newImage");

        Long userId = userService.signIn(signInRequest).getId();

        UpdateRequest request = new UpdateRequest();
        request.setNickname("newNick");
        request.setFile(file);

        //when
        userService.updateProfile(userId, request);

        //then
        User updated = userRepository.findById(userId).orElseThrow();
        assertEquals("newNick", updated.getNickname());
        assertEquals("newImage", updated.getImageUrl());
    }

    @Test
    @DisplayName("회원의 비밀번호 변경 요청은 저장된 비밀번호를 갱신한다.")
    void changePassword() {
        //given
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image".getBytes());
        SignInRequest signInRequest = createSignInRequest(1, file);
        when(fileStorageService.save(file)).thenReturn("newImage");

        Long userId = userService.signIn(signInRequest).getId();

        PasswordUpdateRequest request = new PasswordUpdateRequest();
        request.setPassword("NewPassword1!");

        //when
        userService.changePassword(userId, request);

        //then
        User updated = userRepository.findById(userId).orElseThrow();
        assertEquals("NewPassword1!", updated.getPassword());
    }

    @Test
    @DisplayName("회원 탈퇴 시 프로필 이미지가 삭제되고 회원 데이터가 제거된다.")
    void deleteUser() {
        //given
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "image".getBytes());
        SignInRequest signInRequest = createSignInRequest(1, file);
        when(fileStorageService.save(file)).thenReturn("imageUrl");

        Long userId = userService.signIn(signInRequest).getId();

        //when
        userService.deleteUser(userId);

        //then
        assertTrue(userRepository.findById(userId).isEmpty());
        verify(fileStorageService).delete("imageUrl");
    }

    private SignInRequest createSignInRequest(int sequence, MultipartFile file) {
        SignInRequest request = new SignInRequest();
        request.setEmail("e" + sequence + "@email.com");
        request.setPassword("Password!" + sequence);
        request.setNickname("nick" + sequence);
        request.setFile(file);
        return request;
    }
}
