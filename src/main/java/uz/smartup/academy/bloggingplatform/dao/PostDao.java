package uz.smartup.academy.bloggingplatform.dao;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import uz.smartup.academy.bloggingplatform.entity.*;

import java.time.LocalDateTime;
import java.util.List;

public interface PostDao {
    void save(Post post);

    void update(Post post);

    void delete(Post post);

    Post getById(int id);

    List<Post> getAllPosts();

    List<Post> getPostsByTag(Tag tag);

    List<Post> getPostsByCategory(Category category);

    User getAuthorById(int id);

    List<Post> getPostsByAuthor(int authorId);

    List<Comment> getPostComments(int id);

    List<Post> findPostsByStatus(Post.Status status);

    List<Post> findPostByStatusAndAuthorId(Post.Status status, int authorId);

    Post.Status findPostStatusById(int postId);

    List<Post> searchPosts(String keyword);

    Page<Post> findPosts(Pageable pageable, Post.Status status, String category, String tag, String keyword);

    List<Post> findDraftsScheduledForPublish(LocalDateTime now, Post.Status status);

    void saveSchedule(PostSchedule postSchedule);

    LocalDateTime getScheduleDateByPostId(int postId);

    PostSchedule getScheduleByPostId(int postId);

    void saveSchedulePost(PostSchedule postSchedule);

    List<Post> findPostsNeedingNotification();

    void deleteScheduleData(PostSchedule postSchedule);
}

/*
post:
    save
    update
    delete
    getById
    getPostsByTag
    getByPostsByCategory
    getAuthorById
    getPostsByAuthor
 */
