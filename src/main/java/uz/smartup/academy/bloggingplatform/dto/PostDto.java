package uz.smartup.academy.bloggingplatform.dto;

import lombok.Getter;
import lombok.Setter;
import uz.smartup.academy.bloggingplatform.entity.Post;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Getter
@Setter
public class PostDto {

    private int id;

    private String title;

    private byte[] photo;

    private String content;

    private LocalDateTime createdAt;

    private boolean isLiked;

    private long likesCount;

    private Post.Status status;

    private List<Integer> categories;

    private List<Integer> tags;

    private String tagsString;

    private String hashedPhoto;

    private LocalDateTime scheduleTime;

    public PostDto() {}

    public PostDto(Builder builder) {
        this.id = builder.id;
        this.title = builder.title;
        this.content = builder.content;
        this.photo = builder.photo;
        this.createdAt = builder.createdAt;
        this.likesCount = builder.likesCount;
        this.status = builder.status;
    }

    public static class Builder {
        private int id;

        private String title;

        private byte[] photo;

        private String content;

        private LocalDateTime createdAt;

        private long likesCount;

        private Post.Status status;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder title(String title) {
            this.title = title;
            return this;
        }

        public Builder photo(byte[] photo) {
            this.photo = photo;
            return this;
        }

        public Builder content(String content) {
            this.content = content;
            return this;
        }

        public Builder createdAt(LocalDateTime createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder likesCount(long likesCount) {
            this.likesCount = likesCount;
            return this;
        }

        public Builder status(Post.Status status) {
            this.status = status;
            return this;
        }

        public PostDto build() {
            return new PostDto(this);
        }

    }

    @Override
    public String toString() {
        return "PostDto{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", photo=" + Arrays.toString(photo) +
                ", content='" + content + '\'' +
                ", createdAt=" + createdAt +
                ", likesCount=" + likesCount +
                '}';
    }
}
