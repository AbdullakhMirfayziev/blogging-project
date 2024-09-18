package uz.smartup.academy.bloggingplatform.service;

import uz.smartup.academy.bloggingplatform.dao.CommentDao;
import uz.smartup.academy.bloggingplatform.dto.CommentDTO;
import uz.smartup.academy.bloggingplatform.entity.Comment;

import java.util.List;

public interface CommentService {
    void save(CommentDTO comment);

    CommentDTO getComment(int id);

    List<CommentDTO> getAllComments();

    void deleteComment(int id);

    void updateComment(CommentDTO comment);


}
