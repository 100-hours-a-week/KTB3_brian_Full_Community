package com.community.global.security;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.model.Post;
import com.community.domain.board.repository.PostRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
@RequiredArgsConstructor
public class PostPermissionEvaluator implements TargetAwarePermissionEvaluator {

    private final PostRepository postRepository;

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        Post post = postRepository.findById((Long) targetId).orElseThrow(
                () -> new CustomException(ErrorCode.POST_NOT_FOUND)
        );
        Long userId = ((AuthenticatedUser) auth.getPrincipal()).userId();

        return post.getUser().getId().equals(userId);
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public String supportType() {
        return TARGET_TYPES.POST.name();
    }
}

