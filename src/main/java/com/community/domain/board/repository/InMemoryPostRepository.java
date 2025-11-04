package com.community.domain.board.repository;

import com.community.domain.board.model.Post;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.common.util.PageUtil;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Field;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Repository
public class InMemoryPostRepository implements PostRepository {

    private static final Map<Long, Post> store = new ConcurrentHashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Long save(Post post) {
        if (post.getId() == null) {
            long newId = sequence.incrementAndGet();
            try{
                Field f = Post.class.getDeclaredField("id");
                f.setAccessible(true);
                f.set(post, newId);
            } catch(Exception e){
                throw new CustomException(ErrorCode.ID_REFLECTION_PROCESSING_FAILED);
            }
        }
        store.put(post.getId(), post);
        return post.getId();
    }

    @Override
    public void delete(Post post) {
        store.remove(post.getId());
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return Optional.ofNullable(store.get(postId));
    }

    @Override
    public PageResult<Post> findAll(PaginationRequest paginationRequest) {
        List<Post> posts = new ArrayList<>(store.values());

        return PageUtil.paginate(posts, paginationRequest);
    }

    @Override
    public List<Post> findAllByUserId(Long userId) {
        return store.values().stream()
                .filter(post -> post.getUser().getId().equals(userId))
                .toList();
    }

    @Override
    public void increaseViewCount(Long postId, long increment) {
        if (increment <= 0) {
            return;
        }
        store.computeIfPresent(postId, (id, post) -> {
            post.addViewCount(increment);
            return post;
        });
    }
}
