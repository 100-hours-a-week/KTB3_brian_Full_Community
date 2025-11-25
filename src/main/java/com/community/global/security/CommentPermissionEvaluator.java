package com.community.global.security;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.model.Comment;
import com.community.domain.board.repository.CommentRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
@RequiredArgsConstructor
public class CommentPermissionEvaluator implements TargetAwarePermissionEvaluator {

    private final CommentRepository commentRepository;


    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        Comment comment = commentRepository.findById((Long) targetId).orElseThrow(
                () -> new CustomException(ErrorCode.COMMENT_NOT_FOUND)
        );

        if (!(permission instanceof List<?> params) || params.size() != 2) {
            return false;
        }

        Long postId = ((Number) params.get(1)).longValue();
        Long userId = ((AuthenticatedUser) auth.getPrincipal()).userId();

        return comment.getUser().getId().equals(userId) && comment.getPost().getId().equals(postId);
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        return false;
    }

    @Override
    public String supportType() {
        return TARGET_TYPES.COMMENT.name();
    }
}
