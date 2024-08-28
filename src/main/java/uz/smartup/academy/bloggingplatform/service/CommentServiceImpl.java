package uz.smartup.academy.bloggingplatform.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import uz.smartup.academy.bloggingplatform.dao.CommentDao;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dao.UserDao;
import uz.smartup.academy.bloggingplatform.dto.CommentDTO;
import uz.smartup.academy.bloggingplatform.dto.CommentDtoUtil;
import uz.smartup.academy.bloggingplatform.entity.Comment;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommentServiceImpl implements CommentService {
   private final CommentDtoUtil dtoUtil;
   private final CommentDao commentDao;
   private final UserDao userDao;
   private final PostDao postDao;
   private final NotificationService notificationService;
    private final MailSenderServiceImpl mailSenderServiceImpl;

    public CommentServiceImpl(CommentDtoUtil dtoUtil, CommentDao commentDao, UserDao userDao, PostDao postDao, NotificationService notificationService, MailSenderServiceImpl mailSenderServiceImpl) {
        this.dtoUtil = dtoUtil;
        this.commentDao = commentDao;
        this.userDao = userDao;
        this.postDao = postDao;
        this.notificationService = notificationService;
//        this.notificationService = notificationService;
        this.mailSenderServiceImpl = mailSenderServiceImpl;
    }

    @Transactional
    @Override
    public void save(CommentDTO commentDTO) {
        Comment comment = dtoUtil.toEntity(commentDTO);
        comment.setAuthor(userDao.getUserById(commentDTO.getAuthorId()));
        comment.setPost(postDao.getById(commentDTO.getPostId()));
        comment.setCreatedAt(LocalDateTime.now());

        User commenter = userDao.getUserById(commentDTO.getAuthorId());
        Post post = postDao.getById(commentDTO.getPostId());
          mailSenderServiceImpl.sendPostCommentEmail(post.getAuthor().getEmail(), post.getAuthor().getUsername(), post.getId(), commenter.getUsername());
        notificationService.addNotification(post.getAuthor().getId(), commenter.getUsername() + " commented your post", "/posts/" + post.getId(), "like");
        System.out.println("ishladi shekilli");

        commentDao.save(comment);
    }

    @Override
    public CommentDTO getComment(int id) {
        return dtoUtil.toDto(commentDao.getComment(id));
    }

    @Override
    public List<CommentDTO> getAllComments() {
        List<Comment> comments = commentDao.getComments();
            return dtoUtil.toDTOs(comments).stream()
                    .sorted((c1, c2) -> c2.getCreatedAt().compareTo(c1.getCreatedAt()))
                    .collect(Collectors.toList());
    }
    @Transactional
    @Override
    public void deleteComment(int id) {
        Comment comment = commentDao.getComment(id);
        commentDao.delete(comment);
    }

    @Transactional
    @Override
    public void updateComment(CommentDTO comment) {
        Comment comment1 = commentDao.getComment(comment.getId());
        System.out.println(comment.getId());
        comment1.setContent(comment.getContent());
        commentDao.update(comment1);
    }
}
