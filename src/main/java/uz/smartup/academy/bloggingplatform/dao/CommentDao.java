package uz.smartup.academy.bloggingplatform.dao;

import uz.smartup.academy.bloggingplatform.entity.Comment;

import java.util.List;

public interface CommentDao {

    void save(Comment comment);

    void update(Comment comment);

    void delete(Comment comment);

    Comment getById(int id);

    Comment getComment(int id);
    List<Comment> getComments();
    List<Comment> getCommentsByPostId(int id);

    List<Comment> findNewComments();
}

/*
Comment:
    save
    update
    delete
    getById
    getCommentsByPostId
 */
