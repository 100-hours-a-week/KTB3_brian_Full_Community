package com.community.domain.user.repository;

import com.community.domain.user.model.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.function.Supplier;

@Primary
@Repository
public class JpaUserRepository implements UserRepository {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Long save(User user) {
        em.persist(user);

        return user.getId();
    }

    @Override
    public void delete(User user) {
        em.remove(user);
    }

    @Override
    public Optional<User> findById(Long userId) {
        return Optional.ofNullable(em.find(User.class, userId));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return fetchSingleResult(() -> em.createQuery("select u from User u where u.email = :email", User.class)
                .setParameter("email", email)
                .getSingleResult());
    }

    @Override
    public Optional<User> findByNickname(String nickName) {
        return fetchSingleResult(() -> em.createQuery("select u from User u where u.nickname = :nickname", User.class)
                .setParameter("nickname", nickName)
                .getSingleResult());
    }

    private <T> Optional<T> fetchSingleResult(Supplier<T> supplier) {
        try {
            return Optional.ofNullable(supplier.get());
        } catch (NoResultException e) {
            return Optional.empty();
        }
    }
}
