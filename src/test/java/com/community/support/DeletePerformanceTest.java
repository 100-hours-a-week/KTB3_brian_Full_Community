package com.community.support;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.service.PostService;
import com.community.domain.file.service.FileStorageService;
import com.community.domain.user.service.UserService;
import com.community.support.PerformanceTestDataGenerator.TestIds;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@Transactional
@ActiveProfiles("test")
class DeletePerformanceTest {

    @Autowired
    PerformanceTestDataGenerator dataGenerator;

    @Autowired
    UserService userService;

    @MockitoBean
    FileStorageService fileStorageService;

    @Autowired
    PostService postService;

    @PersistenceContext
    private EntityManager em;

    @Test
    void measureDeleteUserPerformance() {
        doNothing().when(fileStorageService).delete(any());
        ReflectionTestUtils.setField(postService, "fileStorageService", fileStorageService);

        // 1) 데이터 준비 (숫자는 필요에 따라 조절)
        TestIds ids = dataGenerator.generate(
                1,  // userCount
                20,   // postsPerUser
                20,   // commentsPerPost
                20,    // likesPerPost
                10   // viewsPerPost
        );
        em.flush();
        em.clear();

        Long targetUserId = ids.userId();

        // 3) 실제 측정
        long start = System.nanoTime();
        userService.deleteUser(targetUserId);
        long end = System.nanoTime();

        long elapsedMs = (end - start) / 1_000_000;
        System.out.println("deleteUser(" + targetUserId + ") took " + elapsedMs + " ms");
    }

    @Test
    void measureDeletePostPerformance() {
        TestIds ids = dataGenerator.generate(
                1,
                20,
                10,
                5,
                0
        );

        Long targetUserId = ids.userId();
        Long targetPostId = ids.postId();
        authenticate(targetUserId);

        long start = System.nanoTime();
        postService.deletePost(targetPostId, targetUserId);
        long end = System.nanoTime();

        long elapsedMs = (end - start) / 1_000_000;
        System.out.println("deletePost(" + targetPostId + ") took " + elapsedMs + " ms");
    }

    private void authenticate(Long userId){
        TestingAuthenticationToken testingAuthenticationToken = new TestingAuthenticationToken(new AuthenticatedUser(userId), null, List.of());
        testingAuthenticationToken.setAuthenticated(true);
        SecurityContextHolder.getContext().setAuthentication(testingAuthenticationToken);
    }
}
