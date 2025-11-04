package com.community.domain.board.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Table(name = "post_view_events")
public class PostViewEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_view_event_id")
    private Long id;

    @NotNull
    private Long postId;

    @NotNull
    private LocalDateTime createdAt;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Status status;

    public PostViewEvent(Long postId) {
        this.postId = postId;
        this.createdAt = LocalDateTime.now();
        this.status = Status.PENDING;
    }

    public enum Status {
        PENDING,
        DONE
    }
}
