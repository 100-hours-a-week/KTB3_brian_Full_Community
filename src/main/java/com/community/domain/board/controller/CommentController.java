package com.community.domain.board.controller;

import com.community.domain.auth.annotation.Auth;
import com.community.domain.auth.annotation.AuthUser;
import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.dto.request.CommentRequest;
import com.community.domain.board.dto.response.CommentIdResponse;
import com.community.domain.board.dto.response.CommentSingleResponse;
import com.community.domain.board.service.CommentService;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.common.util.UriUtil;
import com.community.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts/{postId}/comments")
public class CommentController implements CommentApiSpec {

    private final CommentService commentService;

    @Override
    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<CommentSingleResponse>>> getComments(@PathVariable Long postId,
                                                                                        @ModelAttribute PaginationRequest pageRequest) {
        PageResponse<CommentSingleResponse> res = commentService.getComments(postId, pageRequest);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("게시글 댓글 조회에 성공했습니다.", res));
    }

    @Override
    @Auth
    @PostMapping
    public ResponseEntity<ApiResponse<CommentIdResponse>> createComment(@PathVariable Long postId,
                                                                        @AuthUser AuthenticatedUser authenticatedUser,
                                                                        @RequestBody @Valid CommentRequest request) {

        CommentIdResponse res = commentService.createComment(postId, authenticatedUser.userId(), request);

        return ResponseEntity
                .created(UriUtil.makeLocationFromCurrent(res.getId()))
                .body(ApiResponse.success("게시글 댓글 등록에 성공했습니다.", res));
    }

    @Override
    @Auth
    @PutMapping("/{commentId}")
    public ResponseEntity<ApiResponse<CommentIdResponse>> updateComment(@PathVariable Long postId,
                                                                        @PathVariable Long commentId,
                                                                        @AuthUser AuthenticatedUser authenticatedUser,
                                                                        @RequestBody @Valid CommentRequest request) {
        CommentIdResponse response = commentService.updateComment(postId, commentId, authenticatedUser.userId(), request);

        return ResponseEntity
                .ok()
                .body(ApiResponse.success("게시글 댓글 수정에 성공했습니다.", response));
    }

    @Override
    @Auth
    @DeleteMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long postId,
                                                           @PathVariable Long commentId,
                                                           @AuthUser AuthenticatedUser authenticatedUser) {
        commentService.deleteComment(postId, commentId, authenticatedUser.userId());

        return ResponseEntity
                .status(HttpStatus.NO_CONTENT)
                .body(ApiResponse.success("게시글 댓글 삭제에 성공했습니다."));
    }
}
