package com.community.domain.board.model;

import com.community.domain.common.model.BaseTimeEntity;
import com.community.domain.user.model.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "posts")
public class Post extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @NotBlank
    @Size(max = 26)
    private String title;

    @Size(max = 255)
    private String imageUrl;

    @NotBlank
    @Lob
    private String body;

    private Long viewCount;

    public Post(User user, String title, String imageUrl, String body) {
        this.user = user;
        this.title = title;
        this.imageUrl = imageUrl;
        this.body = body;
        this.viewCount = 0L;
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void updateBody(String body) {
        this.body = body;
    }

    public void addViewCount() {
        addViewCount(1);
    }

    public void addViewCount(long increment) {
        if (increment <= 0) {
            return;
        }
        if (this.viewCount == null) {
            this.viewCount = 0L;
        }
        this.viewCount += increment;
    }

    public long getViewCount() {
        return this.viewCount;
    }
}
