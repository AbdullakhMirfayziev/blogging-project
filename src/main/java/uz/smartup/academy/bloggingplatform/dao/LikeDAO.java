package uz.smartup.academy.bloggingplatform.dao;

import uz.smartup.academy.bloggingplatform.entity.Like;
import uz.smartup.academy.bloggingplatform.entity.Notification;
import uz.smartup.academy.bloggingplatform.entity.Post;

import java.util.List;

public interface LikeDAO {

    Like findByUserAndPost(int userId, int postId);

    void save(Like like);

    void delete(Like like);

    long countByPostId(int postId);

    List<Like> getAllLike();

    List<Like> getLikesByPostId(int postId);

    List<Like> findNewLikesByPostId(int postId);

    List<Notification> findNewLikes();
}



/*
    like:
        save
        delete
        getLikesByPostId
 */
