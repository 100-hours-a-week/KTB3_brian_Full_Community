package com.community.domain.board.service;

import com.community.domain.board.dto.request.PostCreateRequest;
import com.community.domain.board.dto.request.PostUpdateRequest;
import com.community.domain.board.dto.response.*;
import com.community.domain.board.model.Post;
import com.community.domain.board.model.PostLike;
import com.community.domain.board.repository.PostLikeRepository;
import com.community.domain.board.repository.PostRepository;
import com.community.domain.common.page.PageResponse;
import com.community.domain.common.page.PageResult;
import com.community.domain.common.page.PaginationRequest;
import com.community.domain.file.service.FileStorageService;
import com.community.domain.user.model.User;
import com.community.domain.user.repository.UserRepository;
import com.community.global.exception.CustomException;
import com.community.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final PostLikeRepository postLikeRepository;
    private final FileStorageService fileStorageService;
    private final CommentService commentService;
    private final PostViewEventService postViewEventService;

    @Transactional(readOnly = true)
    public PageResponse<PostSingleResponse> getPostList(PaginationRequest paginationRequest) {
        PageResult<Post> pageResult = postRepository.findAll(paginationRequest);

        List<PostSingleResponse> items = pageResult.items().stream()
                .map(this::toSingleResponse)
                .toList();

        return new PageResponse<>(
                items,
                pageResult.totalElements(),
                pageResult.totalPages(),
                paginationRequest.page(),
                paginationRequest.size());
    }

    public PostSingleResponse viewPost(Long postId) {
        Post post = findPost(postId);
        postViewEventService.addEvent(postId);

        return toSingleResponse(post);
    }

    public PostIdResponse createPost(Long userId, PostCreateRequest req) {
        String imageUrl = fileStorageService.save(req.getFile());
        User user = userRepository.findById(userId).orElseThrow(
                () -> new CustomException(ErrorCode.NOT_FOUND_USER)
        );
        Post post = new Post(user, req.getTitle(), imageUrl, req.getBody());
        Long id = postRepository.save(post);

        return new PostIdResponse(id);
    }

    public PostIdResponse updatePost(Long postId, Long userId, PostUpdateRequest request) {
        Post post = findPost(postId);
        validateAuthor(post, userId);

        post.updateTitle(request.getTitle());
        post.updateBody(request.getBody());

        MultipartFile image = request.getFile();
        if (image != null && !image.isEmpty()) {
            String previousImageUrl = post.getImageUrl();
            String imageUrl = fileStorageService.save(image);
            post.updateImageUrl(imageUrl);
            fileStorageService.delete(previousImageUrl);
        }

        return new PostIdResponse(post.getId());
    }

    public void deletePost(Long postId, Long userId) {
        Post post = findPost(postId);
        validateAuthor(post, userId);
        fileStorageService.delete(post.getImageUrl());
        postLikeRepository.deleteAllByPostId(postId);
        commentService.deleteAllCommentByPostId(postId);
        postRepository.delete(post);
    }

    public void deleteAllPostByUserId(Long userId) {
        List<Post> postList = postRepository.findAllByUserId(userId);

        for (Post post : postList) {
            deletePost(post.getId(), userId);
        }
    }

    public PostLikeResponse toggleLike(Long postId, Long userId) {
        Post post = findPost(postId);
        User user = findUser(userId);
        Optional<PostLike> postLike = postLikeRepository.findByPostIdAndUserId(postId, userId);

        boolean liked = postLike.isPresent();
        if (liked) {
            postLikeRepository.delete(postLike.get());
        } else {
            postLikeRepository.save(new PostLike(post, user));
        }

        return new PostLikeResponse(!liked);
    }

    @Transactional(readOnly = true)
    public PostLikeResponse checkUserLikedPost(Long postId, Long userId) {
        findPost(postId);
        boolean liked = postLikeRepository.existsByPostIdAndUserId(postId, userId);

        return new PostLikeResponse(liked);
    }

    private AuthorResponse getAuthorResponse(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

        return AuthorResponse.from(user);
    }

    private PostSingleResponse toSingleResponse(Post post) {
        long likeCount = postLikeRepository.countByPostId(post.getId());

        Long commentCount = commentService.countComments(post.getId());

        return new PostSingleResponse(
                PostContent.from(post, likeCount, commentCount),
                getAuthorResponse(post.getUser().getId())
        );
    }

    private Post findPost(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }

    private void validateAuthor(Post post, Long userId) {
        if (!post.getUser().getId().equals(userId)) {
            throw new CustomException(ErrorCode.POST_FORBIDDEN);
        }
    }
}
