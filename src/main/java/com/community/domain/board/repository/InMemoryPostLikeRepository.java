package com.community.domain.board.repository;

import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostLikeRepository implements PostLikeRepository {

    private static final Map<Long, PostLike> store = new ConcurrentHashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Long save(PostLike postLike) {
        if (postLike.getId() == null) {
            long newId = sequence.incrementAndGet();
            try {
                Field f = Post.class.getDeclaredField("id");
                f.setAccessible(true);
                f.set(postLike, newId);
            } catch (Exception e) {
                throw new CustomException(ErrorCode.ID_REFLECTION_PROCESSING_FAILED);
            }
        }
        store.put(postLike.getId(), postLike);
        return postLike.getId();
    }

    @Override
    public void delete(PostLike postLike) {
        store.remove(postLike.getId());
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        store.entrySet().removeIf(entry -> entry.getValue().getPost().getId().equals(postId));
    }

    @Override
    public Boolean existsByPostIdAndUserId(Long postId, Long userId) {
        return store.values().stream().anyMatch(postLike ->
                postLike.getPost().getId().equals(postId) && postLike.getUser().getId().equals(userId));
    }

    @Override
    public Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId) {
        return store.values().stream().filter(postLike ->
                postLike.getPost().getId().equals(postId) && postLike.getUser().getId().equals(userId)).findFirst();
    }

    @Override
    public Long countByPostId(Long postId) {
        return store.values().stream()
                .filter(like -> like.getPost().getId().equals(postId))
                .count();
    }
}
