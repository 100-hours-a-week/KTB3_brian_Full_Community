package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Schema(description = "댓글 내용 응답 DTO")
@Getter
@AllArgsConstructor
public class CommentContent {

    private final Long id;
    private final String body;
    private final LocalDateTime updatedAt;
}
