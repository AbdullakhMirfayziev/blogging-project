package uz.smartup.academy.bloggingplatform.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

@Getter
@Setter
public class LikeDTO {
    private int id;
    private int userId;
    private int postId;
    private Timestamp createdAt;

    public LikeDTO(Builder builder) {
        this.id = builder.id;
        this.userId = builder.userId;
        this.postId = builder.postId;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private int id;
        private int userId;
        private int postId;
        private Timestamp createdAt;

        public Builder id(int id) {
            this.id = id;
            return this;
        }

        public Builder userId(int userId) {
            this.userId = userId;
            return this;
        }

        public Builder postId(int postId) {
            this.postId = postId;
            return this;
        }

        public Builder createdAt(Timestamp createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public LikeDTO build() {
            return new LikeDTO(this);
        }
    }
}