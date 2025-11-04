package com.community.domain.board.repository;

import com.community.domain.board.model.PostLike;

import java.util.Optional;

public interface PostLikeRepository {

    Long save(PostLike postLike);

    void delete(PostLike postLike);

    void deleteAllByPostId(Long postId);

    Boolean existsByPostIdAndUserId(Long postId, Long userId);

    Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId);

    Long countByPostId(Long postId);
}
