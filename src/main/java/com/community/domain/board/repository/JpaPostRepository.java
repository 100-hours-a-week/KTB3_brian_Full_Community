package com.community.domain.board.repository;

import com.community.domain.board.model.Post;
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
public class JpaPostRepository implements PostRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Long save(Post post) {
        em.persist(post);

        return post.getId();
    }

    @Override
    public void delete(Post post) {
        em.remove(post);
    }

    @Override
    public Optional<Post> findById(Long postId) {
        return Optional.ofNullable(em.find(Post.class, postId));
    }

    @Override
    public PageResult<Post> findAll(PaginationRequest paginationRequest) {
        int page = paginationRequest.page();
        int size = paginationRequest.size();
        int offset = page * size;

        String sortProperty = resolveSortProperty(paginationRequest.sortBy());
        String direction = PageUtil.resolveDirection(paginationRequest.direction());

        String query = "select p from Post p order by p." + sortProperty + " " + direction;
        List<Post> posts = em.createQuery(query, Post.class)
                .setFirstResult(offset)
                .setMaxResults(size)
                .getResultList();

        Long totalElements = em.createQuery("select count(p) from Post p", Long.class)
                .getSingleResult();

        int totalPages = PageUtil.calculateTotalPages(totalElements, size);

        return new PageResult<>(posts, totalElements, totalPages);
    }

    @Override
    public List<Post> findAllByUserId(Long userId) {
        return em.createQuery("select p from Post p join fetch p.user u where u.id = :userId order by p.createdAt desc", Post.class)
                .setParameter("userId", userId)
                .getResultList();
    }

    @Override
    public void increaseViewCount(Long postId, long increment) {
        if (increment <= 0) {
            return;
        }
        em.createQuery("update Post p set p.viewCount = p.viewCount + :increment where p.id = :postId")
                .setParameter("increment", increment)
                .setParameter("postId", postId)
                .executeUpdate();
    }

    private String resolveSortProperty(String sortBy) {
        String normalized = sortBy.trim().toLowerCase();

        return switch (normalized) {
            case "viewcount" -> "viewCount";
            case "title" -> "title";
            case "createdat" -> "createdAt";
            default -> "createdAt";
        };
    }
}
