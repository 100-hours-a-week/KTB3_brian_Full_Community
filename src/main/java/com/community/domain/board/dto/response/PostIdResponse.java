package com.community.domain.board.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "Post Id 만을 포함하는 응답 DTO")
@Getter
@AllArgsConstructor
public class PostIdResponse {

    private final Long id;
}
