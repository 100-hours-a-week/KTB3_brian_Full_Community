package com.community.helper;

import com.community.domain.board.model.Comment;
import com.community.domain.board.model.Post;
import com.community.domain.user.model.User;

public abstract class CommentMaker {

    public static Comment getNumberedComment(Post post, User user, int number) {
        return new Comment(post, user, "comment body " + number);
    }
}
