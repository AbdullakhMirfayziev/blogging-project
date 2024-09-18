package uz.smartup.academy.bloggingplatform.rest;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.smartup.academy.bloggingplatform.dto.CommentDTO;
import uz.smartup.academy.bloggingplatform.dto.PostDto;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.service.PostService;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostApi {

    private final PostService postService;

    public PostApi(PostService postService) {
        this.postService = postService;
    }

    @GetMapping("/{id}")
    public PostDto getPostById(@PathVariable("id") int id) {
        PostDto post = postService.getById(id);

        if(post != null) return post;
        else throw new RuntimeException("Post not found by id :" + id);
    }

    @GetMapping
    public List<PostDto> getPosts() {
        List<PostDto> posts = postService.getAllPosts();

        if(!posts.isEmpty()) return posts;
        else throw new RuntimeException("Any post doesn't exist yet");
    }

    @GetMapping("/{id}/comments")
    public List<CommentDTO> getPostComments(@PathVariable("id") int id) {
        List<CommentDTO> comments = postService.getPostComments(id);

        if(!comments.isEmpty()) return comments;
        else throw new RuntimeException("Any comment doesn't exist yet");
    }

    @GetMapping("/drafts")
    public List<PostDto> getDraftPosts() {
        List<PostDto> posts = postService.getDraftPost();

        if(!posts.isEmpty()) return posts;
        else throw new RuntimeException("Any post doesn't exist yet");
    }

    @GetMapping("/published")
    public List<PostDto> getPublishedPosts() {
        List<PostDto> posts = postService.getPublishedPost();

        if(!posts.isEmpty()) return posts;
        else throw new RuntimeException("Any post doesn't exist yet");
    }

    @GetMapping("/{postId}/like-count")
    public ResponseEntity<Post> getPostWithLikeCount(@PathVariable int postId) {
        Post post = postService.getPostWithLikeCount(postId);
        return ResponseEntity.ok(post);
    }
  
    @PostMapping("{postId}/addComment/{userId}")
    public void addComment(@PathVariable int postId, @PathVariable int userId ,@RequestBody CommentDTO comment) {
        postService.addCommentToPost(userId, postId, comment);
    }

    @GetMapping("/categories/{categoryTitle}")
    public List<PostDto> getPostsByCategory(@PathVariable("categoryTitle") String  categoryTitle) {
        List<PostDto> posts = postService.getPostsByCategory(categoryTitle);

        if(posts.isEmpty()) throw new RuntimeException("sorry, posts don't exist yet");
        else return posts;
    }

    @GetMapping("/tags/{tagTitle}")
    public List<PostDto> getPostsByTag(@PathVariable("tagTitle") String tagTitle) {
        List<PostDto> posts = postService.getPostsByTag(tagTitle);

        if(posts.isEmpty()) throw new RuntimeException("sorry, posts don't exist yet");
        else return posts;
    }



}
