package com.community.domain.board.controller;

import com.community.domain.auth.dto.AuthenticatedUser;
import com.community.domain.board.dto.request.CommentRequest;
import com.community.domain.board.dto.response.CommentIdResponse;
import com.community.domain.board.dto.response.CommentSingleResponse;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PaginationRequest;
import com.community.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Comment", description = "댓글 관리 API")
public interface CommentApiSpec {

    @Operation(summary = "댓글 리스트 조회", description = "페이징을 기반으로 댓글 리스트를 조회합니다.")
    ResponseEntity<ApiResponse<PageResponse<CommentSingleResponse>>> getComments(Long postId, PaginationRequest paginationRequest);

    @Operation(summary = "댓글 추가", description = "특정 게시글에 댓글을 작성합니다.")
    ResponseEntity<ApiResponse<CommentIdResponse>> createComment(Long postId,
                                                                 AuthenticatedUser authenticatedUser,
                                                                 CommentRequest request);

    @Operation(summary = "댓글 수정", description = "특정 게시글에 달려있는 특정 댓글을 수정합니다.")
    ResponseEntity<ApiResponse<CommentIdResponse>> updateComment(Long postId,
                                                                 Long commentId,
                                                                 AuthenticatedUser authenticatedUser,
                                                                 CommentRequest request);

    @Operation(summary = "댓글 삭제", description = "특정 게시글에 달려있는 특정 댓글을 삭제합니다.")
    ResponseEntity<ApiResponse<Void>> deleteComment(Long postId,
                                                    Long commentId,
                                                    AuthenticatedUser authenticatedUser);
}
