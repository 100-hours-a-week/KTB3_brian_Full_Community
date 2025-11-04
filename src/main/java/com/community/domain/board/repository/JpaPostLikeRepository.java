package com.community.domain.board.repository;

import com.community.domain.board.model.PostLike;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Primary
@Repository
public class JpaPostLikeRepository implements PostLikeRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Long save(PostLike postLike) {
        em.persist(postLike);

        return postLike.getId();
    }

    @Override
    public void delete(PostLike postLike) {
        em.remove(postLike);
    }

    @Override
    public void deleteAllByPostId(Long postId) {
        em.createQuery("delete from PostLike p where p.post.id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();
    }

    @Override
    public Boolean existsByPostIdAndUserId(Long postId, Long userId) {
        Long result = em.createQuery("select count(p) from PostLike p where p.post.id = :postId and p.user.id = :userId", Long.class)
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getSingleResult();

        return result > 0;
    }

    @Override
    public Optional<PostLike> findByPostIdAndUserId(Long postId, Long userId) {
        return em.createQuery("select p from PostLike p where p.post.id = :postId and p.user.id = :userId", PostLike.class)
                .setParameter("postId", postId)
                .setParameter("userId", userId)
                .getResultStream().findFirst();
    }

    @Override
    public Long countByPostId(Long postId) {
        return em.createQuery("select count(p) from PostLike p where p.post.id = :postId", Long.class)
                .setParameter("postId", postId)
                .getSingleResult();
    }
}
