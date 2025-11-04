package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "현재 회원의 게시글 좋아요 여부 응답 DTO")
@Getter
@AllArgsConstructor
public class PostLikeResponse {

    private final boolean liked;
}
