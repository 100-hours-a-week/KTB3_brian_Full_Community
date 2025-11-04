package com.community.domain.board.service;

import com.community.domain.board.model.PostViewEvent;
import com.community.domain.board.repository.JpaPostViewEventRepository;
import com.community.domain.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class PostViewEventService {

    private static final int BATCH_SIZE = 100;

    private final JpaPostViewEventRepository jpaPostViewEventRepository;
    private final PostRepository postRepository;

    public void addEvent(Long postId) {
        jpaPostViewEventRepository.save(new PostViewEvent(postId));
    }

    @Scheduled(fixedDelayString = "${application.post-view-event.consumer-delay-ms}")
    public void consumeBatch() {
        List<PostViewEvent> rows = jpaPostViewEventRepository.findByLimitOrderByIdAsc(BATCH_SIZE);

        if (rows.isEmpty()) {
            return;
        }

        Map<Long, Long> countByPost = new HashMap<>();
        for (PostViewEvent event : rows) {
            countByPost.merge(event.getPostId(), 1L, Long::sum);
        }

        countByPost.forEach(postRepository::increaseViewCount);
        jpaPostViewEventRepository.updateStatus(countByPost.keySet());
    }
}
