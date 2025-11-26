package com.community.domain.user.service;

import com.community.domain.board.service.CommentService;
import com.community.domain.board.service.PostService;
import com.community.domain.file.service.FileStorageService;
import com.community.domain.user.dto.request.PasswordUpdateRequest;
import com.community.domain.user.dto.request.SignInRequest;
import com.community.domain.user.dto.request.UpdateRequest;
import com.community.domain.user.dto.response.SignInAvailableResponse;
import com.community.domain.user.dto.response.SignInResponse;
import com.community.domain.user.dto.response.UserResponse;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private FileStorageService fileStorageService;
    @Mock
    private PostService postService;
    @Mock
    private CommentService commentService;

    @InjectMocks
    private UserService userService;

    @Test
    @DisplayName("회원가입에 성공하면 저장된 회원 ID를 응답한다.")
    void signIn() {
        //given
        ReflectionTestUtils.setField(userService, "DEFAULT_IMAGE_URL", "http://default");
        MultipartFile file = new MockMultipartFile(
                "file",
                "avatar.png",
                "image/png",
                "fake-image-content".getBytes()
        );

        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("email@email.com");
        signInRequest.setPassword("Password1!");
        signInRequest.setNickname("nickname");
        signInRequest.setFile(file);

        when(userRepository.findByEmail(signInRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(signInRequest.getNickname())).thenReturn(Optional.empty());
        when(fileStorageService.save(any(MultipartFile.class))).thenReturn("mockImageURL");
        when(userRepository.save(any())).thenReturn(123L);

        //when
        SignInResponse response = userService.signIn(signInRequest);

        //then
        assertThat(response.getId()).isEqualTo(123L);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getEmail()).isEqualTo(signInRequest.getEmail());
        assertThat(saved.getNickname()).isEqualTo(signInRequest.getNickname());
        assertThat(saved.getImageUrl()).isEqualTo("mockImageURL");
    }

    @Test
    @DisplayName("회원가입 시 프로필 이미지를 업로드하지 않으면 기본 이미지를 사용하고 파일 저장을 호출하지 않는다.")
    void signIn_uses_default_image_when_file_absent() {
        //given
        ReflectionTestUtils.setField(userService, "DEFAULT_IMAGE_URL", "http://default");
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("email@email.com");
        signInRequest.setPassword("Password1!");
        signInRequest.setNickname("nickname");

        when(userRepository.findByEmail(signInRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(signInRequest.getNickname())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(1L);

        //when
        SignInResponse response = userService.signIn(signInRequest);

        //then
        assertThat(response.getId()).isEqualTo(1L);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getImageUrl()).isEqualTo("http://default");
        verify(fileStorageService, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 시 이미지를 추가하지 않으면 기본 이미지를 사용한다.")
    void signIn_use_default_image_when_image_empty() {

        //given
        String defaultImageURL = "http://default";
        ReflectionTestUtils.setField(userService, "DEFAULT_IMAGE_URL", defaultImageURL);
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("e");
        signInRequest.setNickname("n");

        when(userRepository.findByEmail(signInRequest.getEmail())).thenReturn(Optional.empty());
        when(userRepository.findByNickname(signInRequest.getNickname())).thenReturn(Optional.empty());

        //when
        userService.signIn(signInRequest);
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        //then
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertThat(saved.getImageUrl()).isEqualTo(defaultImageURL);
    }


    @Test
    @DisplayName("회원가입 시 이메일이 중복이면 예외를 던진다.")
    void signIn_throws_when_email_duplicate() {
        //given
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("dup@email.com");
        signInRequest.setPassword("Password1!");
        signInRequest.setNickname("nickname");

        when(userRepository.findByEmail(signInRequest.getEmail())).thenReturn(Optional.of(new User("dup@email.com", "p", "n", "i")));

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.signIn(signInRequest));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("회원가입 시 닉네임이 중복이면 예외를 던진다.")
    void signIn_throws_when_nickname_duplicate() {
        //given
        SignInRequest signInRequest = new SignInRequest();
        signInRequest.setEmail("email@email.com");
        signInRequest.setPassword("Password1!");
        signInRequest.setNickname("dupName");

        when(userRepository.findByNickname(signInRequest.getNickname())).thenReturn(Optional.of(new User("e", "p", "dupName", "i")));

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.signIn(signInRequest));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_NICKNAME);

        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 유니크하다면 사용 가능함을 나타내는 객체를 반환한다.")
    void checkEmailAvailability() {
        //given
        String email = "unique@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        //when
        SignInAvailableResponse res = userService.checkEmailAvailability(email);

        //then
        assertTrue(res.getIsSignInInformationAvailable());
    }

    @Test
    @DisplayName("이메일 중복 확인 시 이메일이 중복이면 예외를 던진다.")
    void checkEmailAvailability_throws_when_email_duplicate() {
        //given
        String email = "dup@email.com";
        when(userRepository.findByEmail(email)).thenReturn(Optional.of(new User("dup@email.com", "p", "n", "i")));

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.checkEmailAvailability(email));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_EMAIL);
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 유니크하다면 사용 가능함을 나타내는 객체를 반환한다")
    void checkNicknameAvailability() {
        //given
        String nickname = "uniqueNickname";
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.empty());

        //when
        SignInAvailableResponse res = userService.checkNicknameAvailability(nickname);

        //then
        assertTrue(res.getIsSignInInformationAvailable());
    }

    @Test
    @DisplayName("닉네임 중복 확인 시 닉네임이 중복이면 예외를 던진다.")
    void checkNicknameAvailability_throws_when_nickname_duplicate() {
        //given
        String nickname = "dupNickname";
        when(userRepository.findByNickname(nickname)).thenReturn(Optional.of(new User("e", "p", nickname, "i")));

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.checkNicknameAvailability(nickname));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_NICKNAME);
    }

    @Test
    @DisplayName("userId 로 회원의 프로필 정보를 조회할 수 있다.")
    void getUserProfile() {
        //given
        User user = new User("email@email.com", "pass", "nick", "img");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //when
        ReflectionTestUtils.setField(user, "id", 1L);

        UserResponse response = userService.getUserProfile(1L);

        //then
        assertThat(response.getId()).isEqualTo(1L);
        assertThat(response.getNickname()).isEqualTo(user.getNickname());
        assertThat(response.getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("userId 로 회원의 프로필 정보를 조회할 때, 존재하지 않는 회원이면 예외를 던진다.")
    void getUserProfile_throws_when_user_not_found() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.getUserProfile(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("회원의 프로필 정보를 수정할 수 있다.")
    void updateProfile() {
        //given
        User user = new User("email@email.com", "pass", "oldNick", "oldImage");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByNickname("newNick")).thenReturn(Optional.empty());
        when(fileStorageService.save(any())).thenReturn("newImage");

        UpdateRequest req = new UpdateRequest();
        req.setNickname("newNick");
        req.setFile(new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes()));

        //when
        userService.updateProfile(1L, req);

        //then
        assertThat(user.getNickname()).isEqualTo("newNick");
        assertThat(user.getImageUrl()).isEqualTo("newImage");
        verify(fileStorageService).delete("oldImage");
    }

    @Test
    @DisplayName("회원의 프로필 정보를 수정할 때 닉네임이 그대로면 중복 검사를 하지 않는다.")
    void updateProfile_skips_nickname_validation_when_same() {
        //given
        User user = new User("email@email.com", "pass", "sameNick", "img");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UpdateRequest req = new UpdateRequest();
        req.setNickname("sameNick");

        //when
        userService.updateProfile(1L, req);

        //then
        assertThat(user.getNickname()).isEqualTo("sameNick");
        verify(userRepository, never()).findByNickname(any());
    }

    @Test
    @DisplayName("회원의 프로필 정보를 수정할 때 기존 이미지가 기본 이미지라면 삭제하지 않는다.")
    void updateProfile_does_not_delete_default_image_when_previous_image_default() {
        //given
        String defaultImageUrl = "defaultImageUrl";
        String newImageUrl = "newImageUrl";
        ReflectionTestUtils.setField(userService, "DEFAULT_IMAGE_URL", defaultImageUrl);

        User user = new User("email@email.com", "pass", "nick", defaultImageUrl);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(fileStorageService.save(any())).thenReturn(newImageUrl);

        UpdateRequest req = new UpdateRequest();
        req.setFile(new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes()));

        //when
        userService.updateProfile(1L, req);

        //then
        assertThat(user.getImageUrl()).isEqualTo(newImageUrl);
        verify(fileStorageService, never()).delete(any());
    }

    @Test
    @DisplayName("회원의 프로필 정보를 수정할 때 회원 정보가 존재하지 않으면 예외를 던진다.")
    void updateProfile_throws_when_user_not_found() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        UpdateRequest req = new UpdateRequest();
        req.setNickname("newNick");

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.updateProfile(1L, req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("회원의 프로필 정보를 수정할 때 닉네임이 중복이면 예외를 던진다.")
    void updateProfile_throws_when_nickname_duplicate() {
        //given
        User user = new User("email@email.com", "pass", "oldNick", "img");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByNickname("dupNick")).thenReturn(Optional.of(new User("e", "p", "dupNick", "i")));

        UpdateRequest req = new UpdateRequest();
        req.setNickname("dupNick");

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.updateProfile(1L, req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.DUPLICATED_NICKNAME);
        assertThat(user.getNickname()).isEqualTo("oldNick");
    }

    @Test
    @DisplayName("회원이 프로필 정보를 수정할 때 파일 관련 문제가 생기면 예외를 던진다.")
    void updateProfile_throws_when_file_error() {
        //given
        User user = new User("email@email.com", "pass", "nick", "img");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        MultipartFile file = new MockMultipartFile("file", "avatar.png", "image/png", "img".getBytes());
        UpdateRequest req = new UpdateRequest();
        req.setFile(file);

        when(fileStorageService.save(any())).thenThrow(new CustomException(ErrorCode.FILE_STORAGE_ERROR));

        //when + then
        assertThrows(CustomException.class, () -> userService.updateProfile(1L, req));
    }

    @Test
    @DisplayName("회원의 비밀번호를 수정할 수 있다.")
    void changePassword() {
        //given
        String newPw = "new";
        User user = new User("e", "old", "n", "i");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        PasswordUpdateRequest req = new PasswordUpdateRequest();
        req.setPassword(newPw);

        //when
        userService.changePassword(1L, req);

        //then
        assertThat(user.getPassword()).isEqualTo(newPw);
    }


    @Test
    @DisplayName("회원이 비밀번호를 수정할 때 회원 정보가 존재하지 않으면 예외를 던진다.")
    void changePassword_throws_when_user_not_found() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        PasswordUpdateRequest req = new PasswordUpdateRequest();
        req.setPassword("pw");

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.changePassword(1L, req));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("회원은 탈퇴할 수 있다.")
    void deleteUser() {
        //given
        User user = new User("e", "p", "n", "image");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        //when
        userService.deleteUser(1L);

        //then
        verify(fileStorageService).delete("image");
        verify(postService).deleteAllPostByUserId(1L);
        verify(commentService).deleteAllCommentsByUserId(1L);
        verify(userRepository).delete(user);
    }

    @Test
    @DisplayName("회원이 탈퇴할 때 회원 정보가 존재하지 않으면 예외를 던진다.")
    void deleteUser_throws_when_user_not_found() {
        //given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        //when + then
        CustomException ex = assertThrows(CustomException.class, () -> userService.deleteUser(1L));
        assertThat(ex.getErrorCode()).isEqualTo(ErrorCode.NOT_FOUND_USER);
    }
}
