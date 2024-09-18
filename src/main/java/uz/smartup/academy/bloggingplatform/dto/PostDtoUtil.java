package uz.smartup.academy.bloggingplatform.dto;

import org.springframework.stereotype.Component;
import uz.smartup.academy.bloggingplatform.entity.Post;

import java.util.List;

@Component
public class PostDtoUtil {

    public Post toEntity(PostDto postDto) {
        Post post = new Post();
        post.setId(postDto.getId());
        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setCreatedAt(postDto.getCreatedAt());
        post.setPhoto(postDto.getPhoto());
        post.setLikesCount(postDto.getLikesCount());

        return post;
    }

    public PostDto toDto(Post post) {

        return new PostDto.Builder()
                .id(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .photo(post.getPhoto())
                .createdAt(post.getCreatedAt())
                .likesCount(post.getLikesCount())
                .status(post.getStatus())
                .build();
    }

    public List<PostDto> toDTOs(List<Post> posts) {
        return posts.stream().map(this::toDto).toList();
    }


}
