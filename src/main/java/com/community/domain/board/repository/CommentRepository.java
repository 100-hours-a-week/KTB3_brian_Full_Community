package com.community.domain.board.repository;

import com.community.domain.board.model.Comment;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;

import java.util.Optional;

public interface CommentRepository {

    Long save(Comment comment);
    void delete(Comment comment);
    Optional<Comment> findById(Long commentId);
    PageResult<Comment> findByPostId(Long postId, PaginationRequest paginationRequest);
    Long countByPostId(Long postId);
    void deleteByPostId(Long postId);
}
