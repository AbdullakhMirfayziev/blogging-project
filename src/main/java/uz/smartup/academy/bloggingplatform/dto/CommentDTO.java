package uz.smartup.academy.bloggingplatform.dto;


import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CommentDTO {
    private int id;
    private int authorId;
    private int postId;
    private String content;
    private String username;
    private LocalDateTime createdAt;
    private String hashedPhoto;
    private String edited;
}
