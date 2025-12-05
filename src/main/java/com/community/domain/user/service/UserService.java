package com.community.domain.user.service;

import com.community.domain.board.service.CommentService;
import com.community.domain.file.service.FileStorageService;
import com.community.domain.board.service.PostService;
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
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional
public class UserService {

    @Value("${application.local.default_image_url}")
    private String DEFAULT_IMAGE_URL;

    private final UserRepository userRepository;
    private final FileStorageService fileStorageService;
    private final PostService postService;
    private final CommentService commentService;

    public SignInResponse signIn(SignInRequest req) {
        validateEmailUnique(req.getEmail());
        validateNicknameUnique(req.getNickname());

        MultipartFile file = req.getFile();
        String imageUrl = DEFAULT_IMAGE_URL;
        if (file != null && !file.isEmpty()) {
            imageUrl = fileStorageService.save(file);
        }

        Long savedId = userRepository.save(new User(req.getEmail(), req.getPassword(), req.getNickname(), imageUrl));

        return new SignInResponse(savedId);
    }

    @Transactional(readOnly = true)
    public SignInAvailableResponse checkEmailAvailability(String email) {
        validateEmailUnique(email);

        return new SignInAvailableResponse(true);
    }

    @Transactional(readOnly = true)
    public SignInAvailableResponse checkNicknameAvailability(String nickname) {
        validateNicknameUnique(nickname);

        return new SignInAvailableResponse(true);
    }

    @Transactional(readOnly = true)
    public UserResponse getUserProfile(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        return new UserResponse(user.getId(), user.getEmail(), user.getNickname(), user.getImageUrl());
    }

    public void updateProfile(Long userId, UpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        if (req.getNickname() != null && !req.getNickname().isBlank()
                && !req.getNickname().equals(user.getNickname())) {
            validateNicknameUnique(req.getNickname());
            user.updateNickname(req.getNickname());
        }

        if (req.getFile() != null && !req.getFile().isEmpty()) {
            String previousImageUrl = user.getImageUrl();
            String imageUrl = fileStorageService.save(req.getFile());
            user.updateImageUrl(imageUrl);
            if (!previousImageUrl.equals(DEFAULT_IMAGE_URL)) {
                fileStorageService.delete(previousImageUrl);
            }
        }
    }

    public void changePassword(Long userId, PasswordUpdateRequest req) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        user.updatePassword(req.getPassword());
    }

    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        fileStorageService.delete(user.getImageUrl());

        userRepository.delete(user);
    }

    private void validateEmailUnique(String email) {
        userRepository.findByEmail(email)
                .ifPresent(user -> {
                    throw new CustomException(ErrorCode.DUPLICATED_EMAIL);
                });
    }

    private void validateNicknameUnique(String nickname) {
        userRepository.findByNickname(nickname)
                .ifPresent(user -> {
                    throw new CustomException(ErrorCode.DUPLICATED_NICKNAME);
                });
    }
}
