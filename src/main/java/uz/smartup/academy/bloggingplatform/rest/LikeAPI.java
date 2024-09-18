package uz.smartup.academy.bloggingplatform.rest;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import uz.smartup.academy.bloggingplatform.service.LikeService;

@RestController
@RequestMapping("/api/users/{userId}/posts/{postId}/likes")
public class LikeAPI {
    private final LikeService likeService;

    public LikeAPI(LikeService likeService) {
        this.likeService = likeService;
    }

    @PostMapping
    public ResponseEntity<Void> addLike(@PathVariable int userId, @PathVariable int postId) {
        likeService.addLike(userId, postId);
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @DeleteMapping
    public ResponseEntity<Void> removeLike(@PathVariable int userId, @PathVariable int postId) {
        likeService.removeLike(userId, postId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
