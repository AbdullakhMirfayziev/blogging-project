package uz.smartup.academy.bloggingplatform.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import uz.smartup.academy.bloggingplatform.dao.CommentDao;
import uz.smartup.academy.bloggingplatform.dao.LikeDAO;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.dao.UserDao;
import uz.smartup.academy.bloggingplatform.dto.UserDTO;
import uz.smartup.academy.bloggingplatform.entity.*;
import uz.smartup.academy.bloggingplatform.repository.NotificationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.Timestamp;
import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {


    private final PostDao postDao;
    private final LikeDAO likeDAO;
    private final MailSenderService mailSenderService;
    private final NotificationRepository notificationRepository;
    private final UserDao userDao;
    private final EntityManager entityManager;
    private final CommentDao commentDao;
    private static final Logger logger = LoggerFactory.getLogger(NotificationService.class);

//    private final Logger logger;


    public NotificationService(PostDao postDao, LikeDAO likeDAO, MailSenderService mailSenderService, NotificationRepository notificationRepository, UserDao userDao, EntityManager entityManager, CommentDao commentDao) {
        this.postDao = postDao;
        this.likeDAO = likeDAO;
        this.mailSenderService = mailSenderService;
        this.notificationRepository = notificationRepository;
        this.userDao = userDao;
        this.entityManager = entityManager;
        this.commentDao = commentDao;
    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendLikeNotifications() {

        List<Like> likes = likeDAO.findNewLikes();
        for(Like like : likes){
            User liker = like.getAuthor();
            User postAuthor = like.getPost().getAuthor();
            Post post = like.getPost();
            mailSenderService.sendPostLikedEmail(
                    postAuthor.getEmail(),
                    postAuthor.getUsername(),
                    post.getId(),
                    liker.getUsername()
            );
            like.setNewNotification(false);
            postDao.save(post);

        }

    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendCommentNotifications() {

        List<Comment> comments = commentDao.findNewComments();
        for(Comment comment : comments){
            User commenter = comment.getAuthor();
            User postAuthor = comment.getPost().getAuthor();
            Post post = comment.getPost();
            mailSenderService.sendPostCommentEmail(
                    postAuthor.getEmail(),
                    postAuthor.getUsername(),
                    post.getId(),
                    commenter.getUsername()
            );
            comment.setNewNotification(false);
            postDao.save(post);

        }

    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendPostNotificationsToFollowers() {

        List<Post> posts = postDao.findPostsNeedingNotification();
        for(Post post : posts){
            User author = post.getAuthor();
            List<User> followers = author.getFollowers().stream().toList();
            for(User follower : followers){
                mailSenderService.sendPostNotificationToFollowers(
                        follower.getEmail(),
                        follower.getUsername(),
                        post.getId(),
                        author.getUsername()
                );
            }
            post.setNotification(false);
            postDao.save(post);

        }

    }




    public List<Notification> getUnreadNotifications(int userId) {
        return notificationRepository.findByRecipientIdAndReadFalse(userId);
    }

    public List<Notification> getAllNotification(int userId) {
        return notificationRepository.findAllByRecipientId(userId).reversed();

    }

    public void markAsRead(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void addNotification(int recipientId, String message, String redirectUrl, String type) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(userDao.getUserById(recipientId));
            notification.setMessage(message);
            notification.setRedirectUrl(redirectUrl);
            notification.setRead(false);
            notification.setType(type);
            notification.setCreatedAt(LocalDateTime.now());
            notificationRepository.save(notification);
            logger.info("Notification added for user: {}", recipientId);
        } catch (Exception e) {
            logger.error("Error adding notification", e);
        }
    }




//
//    public List<Notification> findNotificationsByRecipientIdOrderByCreatedAtDesc(int recipientId) {
//        return entityManager.createQuery(
//                        "SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC", Notification.class)
//                .setParameter("recipientId", recipientId)
//                .getResultList();
//    }

}