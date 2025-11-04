package com.community.domain.board.repository;

import com.community.domain.board.model.Comment;
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
public class InMemoryCommentRepository implements CommentRepository {

    private static final Map<Long, Comment> store = new ConcurrentHashMap<>();
    private static final AtomicLong sequence = new AtomicLong(0);

    @Override
    public Long save(Comment comment) {
        if (comment.getId() == null) {
            long newId = sequence.incrementAndGet();
            try{
                Field f = Post.class.getDeclaredField("id");
                f.setAccessible(true);
                f.set(comment, newId);
            } catch(Exception e){
                throw new CustomException(ErrorCode.ID_REFLECTION_PROCESSING_FAILED);
            }
        }
        store.put(comment.getId(), comment);
        return comment.getId();
    }

    @Override
    public void delete(Comment comment) {
        store.remove(comment.getId());
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return Optional.ofNullable(store.get(commentId));
    }

    @Override
    public PageResult<Comment> findByPostId(Long postId, PaginationRequest paginationRequest) {

        List<Comment> source = store.values().stream()
                .filter(comment -> Objects.equals(comment.getPost().getId(), postId))
                .toList();

        return PageUtil.paginate(source, paginationRequest);
    }

    @Override
    public Long countByPostId(Long postId) {
        return store.values().stream()
                .filter(comment -> Objects.equals(comment.getPost().getId(), postId))
                .count();
    }

    @Override
    public void deleteByPostId(Long postId) {
        store.values().removeIf(comment -> Objects.equals(comment.getPost().getId(), postId));
    }
}
