package com.community.domain.board.dto.response;

import com.community.domain.user.model.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Schema(description = "작성자 응답 DTO")
@Getter
@AllArgsConstructor
public class AuthorResponse {

    private final Long id;
    private final String nickname;
    private final String imageUrl;

    public static AuthorResponse from(User user) {
        return new AuthorResponse(user.getId(), user.getNickname(),  user.getImageUrl());
    }
}
