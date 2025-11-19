package com.community.domain.board.repository;

import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Long save(Post post);
    void delete(Post post);
    Optional<Post> findById(Long postId);
    PageResult<Post> findAll(PaginationRequest paginationRequest);
    List<Post> findAllByUserId(Long userId);
    void increaseViewCount(Long postId, long increment);
    PageResult<Post> findByUserId(Long userId, PaginationRequest paginationRequest);
    PageResult<Post> findByPostLikeUserId(Long postId, PaginationRequest paginationRequest);
}
