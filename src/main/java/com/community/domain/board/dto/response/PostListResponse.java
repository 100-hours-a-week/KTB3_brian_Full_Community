package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Schema(description = "Post 리스트 요청 응답 DTO")
@Getter
@AllArgsConstructor
public class PostListResponse {

    private final List<PostSingleResponse> postList;
}
