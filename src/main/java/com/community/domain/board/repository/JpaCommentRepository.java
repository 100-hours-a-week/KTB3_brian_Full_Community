package com.community.domain.board.repository;

import com.community.domain.board.model.Comment;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.common.util.PageUtil;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Primary
@Repository
public class JpaCommentRepository implements CommentRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Long save(Comment comment) {
        em.persist(comment);

        return comment.getId();
    }

    @Override
    public void delete(Comment comment) {
        em.remove(comment);
    }

    @Override
    public Optional<Comment> findById(Long commentId) {
        return Optional.ofNullable(em.find(Comment.class, commentId));
    }

    @Override
    public PageResult<Comment> findByPostId(Long postId, PaginationRequest paginationRequest) {
        int page = paginationRequest.page();
        int size = paginationRequest.size();
        int offset = page * size;

        String sortProperty = paginationRequest.sortBy();
        String direction = PageUtil.resolveDirection(paginationRequest.direction());

        String query = "select c from Comment c where c.post.id =:postId order by c." + sortProperty + " " + direction;
        List<Comment> comments = em.createQuery(query, Comment.class)
                .setParameter("postId", postId)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        Long totalElements = em.createQuery("select count(c) from Comment c where c.post.id = :postId", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();

        int totalPages = PageUtil.calculateTotalPages(totalElements, size);

        return new PageResult<>(comments, totalElements, totalPages);
    }

    @Override
    public Long countByPostId(Long postId) {
        return em.createQuery("select count(c) from Comment c where c.post.id = :postId", Long.class)
                .setParameter("postId", postId).getSingleResult();
    }

    @Override
    public void deleteByPostId(Long postId) {
        em.createQuery("delete from Comment c where c.post.id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();
    }
}
