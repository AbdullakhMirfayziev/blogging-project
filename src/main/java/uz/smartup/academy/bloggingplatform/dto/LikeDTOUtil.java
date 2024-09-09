package uz.smartup.academy.bloggingplatform.dto;

import org.springframework.stereotype.Component;
import uz.smartup.academy.bloggingplatform.entity.Like;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.util.List;

@Component
public class LikeDTOUtil {

    public LikeDTO toDto(Like like) {
        if (like == null) return null;
        return new LikeDTO.Builder()
                .id(like.getId())
                .userId(like.getAuthor().getId())
                .postId(like.getPost().getId())
                .createdAt(like.getCreatedAt())
                .build();
    }

    public Like toEntity(LikeDTO dto, User author, Post post) {
        Like like = new Like();
        like.setId(dto.getId());
        like.setAuthor(author);
        like.setPost(post);
        like.setCreatedAt(dto.getCreatedAt());
        return like;
    }

    public List<LikeDTO> toDTOs(List<Like> likes) {
        return likes.stream().
                map(this::toDto).
                toList();
    }
}
