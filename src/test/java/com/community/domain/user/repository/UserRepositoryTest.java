package com.community.domain.user.repository;


import static com.community.helper.UserMaker.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;

import com.community.domain.user.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
@Import(JpaUserRepository.class)
public class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("새로운 회원을 저장하는 경우 id 를 할당받게 되고, id 를 기준으로 조회할 수 있다.")
    void save_and_findById() {
        //given
        User u = getNumberedUser(1);
        assertNull(u.getId());

        //when
        Long id = repository.save(u);
        Optional<User> found = repository.findById(id);

        //then
        assertNotNull(id);
        assertTrue(found.isPresent());
        assertEquals(u, found.get());
    }

    @Test
    @DisplayName("회원의 필드를 업데이트하면 repository 에 반영된다.")
    void update_and_findById() {
        //given
        User u = getDefaultUser();
        Long id = repository.save(u);
        String newNickName = "newName";

        //when
        u.updateNickname(newNickName);
        entityManager.flush();
        entityManager.clear();

        //then
        User updatedUser = repository.findById(id).orElseThrow();
        assertEquals(newNickName, updatedUser.getNickname());
    }

    @Test
    @DisplayName("조회 결과가 없으면 empty 인 Optional 을 반환한다")
    void findById_NotFound_ReturnsEmpty() {
        assertTrue(repository.findById(999L).isEmpty());
    }

    @Test
    @DisplayName("email 을 통해서 회원을 조회할 수 있다.")
    void save_and_findByEmail() {
        //given
        User u = getDefaultUser();
        String email = u.getEmail();
        repository.save(u);
        entityManager.flush();
        entityManager.clear();

        //when
        Optional<User> found = repository.findByEmail(email);

        //then
        assertTrue(found.isPresent());
        assertEquals(email, found.get().getEmail());
    }

    @Test
    @DisplayName("nickName 을 통해서 회원을 조회할 수 있다.")
    void save_and_findByNickname() {
        //given
        User u = getDefaultUser();
        String nickName = u.getNickname();
        repository.save(u);
        entityManager.flush();
        entityManager.clear();

        //when
        Optional<User> found = repository.findByNickname(nickName);

        //then
        assertTrue(found.isPresent());
        assertEquals(nickName, found.get().getNickname());
    }

    @Test
    @DisplayName("엔티티를 삭제하면 repository 에서 더 이상 조회가 불가능하다")
    void save_and_delete() {
        //given
        User u = getDefaultUser();
        String email = u.getEmail();
        String nickname = u.getNickname();
        Long id = repository.save(u);
        entityManager.flush();

        //when
        repository.delete(u);

        //then
        assertTrue(repository.findById(id).isEmpty());
        assertTrue(repository.findByEmail(email).isEmpty());
        assertTrue(repository.findByNickname(nickname).isEmpty());
    }
}
