package com.community.domain.board.repository;

import com.community.domain.board.model.PostViewEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public class JpaPostViewEventRepository {

    @PersistenceContext
    private EntityManager em;

    public PostViewEvent save(PostViewEvent postViewEvent) {
        em.persist(postViewEvent);
        return postViewEvent;
    }

    public List<PostViewEvent> findByLimitOrderByIdAsc(int limit) {
        return em.createQuery("select e from PostViewEvent e " +
                        "where e.status = :eventType " +
                        "order by e.id asc", PostViewEvent.class)
                .setParameter("eventType", PostViewEvent.Status.PENDING)
                .setFirstResult(0)
                .setMaxResults(limit)
                .getResultList();
    }

    public void updateStatus(Set<Long> ids) {
        em.createQuery("update PostViewEvent e set e.status = :eventType where e.id in :ids")
                .setParameter("eventType", PostViewEvent.Status.DONE)
                .setParameter("ids", ids)
                .executeUpdate();
    }
}
