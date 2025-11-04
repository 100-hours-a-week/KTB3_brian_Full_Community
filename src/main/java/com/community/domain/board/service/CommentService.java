package com.community.domain.board.service;

import com.community.domain.board.dto.request.CommentRequest;

import com.community.domain.board.dto.response.*;

import com.community.domain.board.model.Comment;
import com.community.domain.board.model.Post;
import com.community.domain.board.repository.CommentRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public PageResponse<CommentSingleResponse> getComments(Long postId, PaginationRequest paginationRequest) {
        ensurePostExists(postId);

        PageResult<Comment> pageResult = commentRepository.findByPostId(postId, paginationRequest);

        List<CommentSingleResponse> items = pageResult.items().stream()
                .map(this::toSingleResponse)
                .toList();

        return new PageResponse<>(
                items,
                pageResult.totalElements(),
                pageResult.totalPages(),
                paginationRequest.page(),
                paginationRequest.size());
    }

    public CommentIdResponse createComment(Long postId, Long authorId, CommentRequest request) {
        Post post = postRepository.findById(postId).orElseThrow(
                ()-> new CustomException(ErrorCode.POST_NOT_FOUND)
        );
        User author = userRepository.findById(authorId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );

        Comment comment = new Comment(post, author, request.getBody());
        Long id = commentRepository.save(comment);
        return new CommentIdResponse(id);
    }

    public CommentIdResponse updateComment(Long postId, Long commentId, Long authorId, CommentRequest request) {
        Comment comment = findComment(commentId);
        validateCommentPost(comment, postId);
        validateCommentAuthor(comment, authorId);

        comment.updateBody(request.getBody());
        return new CommentIdResponse(comment.getId());
    }

    public void deleteComment(Long postId, Long commentId, Long authorId) {
        Comment comment = findComment(commentId);
        validateCommentPost(comment, postId);
        validateCommentAuthor(comment, authorId);

        commentRepository.delete(comment);
    }

    public void deleteAllCommentByPostId(Long postId) {
        commentRepository.deleteByPostId(postId);
    }

    @Transactional(readOnly = true)
    public Long countComments(Long postId) {
        return commentRepository.countByPostId(postId);
    }

    private void ensurePostExists(Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));
    }

    private void validateCommentPost(Comment comment, Long postId) {
        if (!comment.getPost().getId().equals(postId)) {
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }
    }

    private void validateCommentAuthor(Comment comment, Long authorId) {
        if (!comment.getUser().getId().equals(authorId)) {
            throw new CustomException(ErrorCode.COMMENT_FORBIDDEN);
        }
    }

    private CommentSingleResponse toSingleResponse(Comment comment) {
        CommentContent commentContent = new CommentContent(
                comment.getId(),
                comment.getBody(),
                comment.getUpdatedAt());

        AuthorResponse authorResponse = toAuthor(comment.getUser().getId());

        return new CommentSingleResponse(commentContent, authorResponse);
    }

    private AuthorResponse toAuthor(Long authorId) {
        User user = userRepository.findById(authorId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        return new AuthorResponse(user.getId(), user.getNickname(), user.getImageUrl());
    }
}
