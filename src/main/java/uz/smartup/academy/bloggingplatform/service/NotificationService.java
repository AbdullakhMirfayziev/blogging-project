package uz.smartup.academy.bloggingplatform.service;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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

        List<Notification> likes = likeDAO.findNewLikes();
        for(Notification like : likes){
            User liker = like.getAuthor();
            int post = like.getPostId();
            User postAuthor = postDao.getById(post).getAuthor();
            mailSenderService.sendPostLikedEmail(
                    postAuthor.getEmail(),
                    postAuthor.getUsername(),
                    post,
                    liker.getUsername()
            );
            like.setNotify(false);
            notificationRepository.save(like);
//            postDao.save(post);

        }

    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendCommentNotifications() {

        List<Notification> comments = commentDao.findNewComments();
        for(Notification comment : comments){
            User commenter = comment.getAuthor();
            int post = comment.getPostId();
            User postAuthor = postDao.getById(post).getAuthor();
            mailSenderService.sendPostCommentEmail(
                    postAuthor.getEmail(),
                    postAuthor.getUsername(),
                    post,
                    commenter.getUsername()
            );
            comment.setNotify(false);
            notificationRepository.save(comment);
//            postDao.save(post);

        }

    }

    @Scheduled(fixedRate = 60000)
    @Transactional
    public void sendPostNotificationsToFollowers() {
        List<Notification> posts = postDao.findPostsNeedingNotification();
        for(Notification post : posts){
            User author = post.getRecipient();
            List<User> followers = author.getFollowers().stream().toList();
            for(User follower : followers){
                mailSenderService.sendPostNotificationToFollowers(
                        follower.getEmail(),
                        follower.getUsername(),
                        post.getId(),
                        author.getUsername()
                );
            }
            post.setNotify(false);
            notificationRepository.save(post);
//            postDao.save(post);

        }

    }

    public List<Notification> getUnreadNotifications(int userId) {
        return notificationRepository.findByRecipientIdAndReadFalse(userId);
    }

    public Page<Notification> getAllNotification(int userId, int size, int page) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Notification> notificationPage = userDao.getAllNotification(pageable, userId);

        return notificationPage;

    }

    public void markAsRead(int notificationId) {
        Notification notification = notificationRepository.findById(notificationId).orElseThrow();
        notification.setRead(true);
        notificationRepository.save(notification);
    }

    public void addNotification(int recipientId, int authorId, int postId, String message, String redirectUrl, NotificationTypes type) {
        try {
            Notification notification = new Notification();
            notification.setRecipient(userDao.getUserById(recipientId));
            notification.setMessage(message);
            notification.setRedirectUrl(redirectUrl);
            notification.setRead(false);
            notification.setType(type);
            notification.setAuthor(userDao.getUserById(authorId));
            notification.setCreatedAt(LocalDateTime.now());
            if(type.equals(NotificationTypes.L) || type.equals(NotificationTypes.C)) notification.setPostId(postId);
            notification.setNotify(type.equals(NotificationTypes.L) || type.equals(NotificationTypes.C) || type.equals(NotificationTypes.N));
            notificationRepository.save(notification);
            logger.info("Notification added for user: {}", recipientId);
        } catch (Exception e) {
            logger.error("Error adding notification", e);
        }
    }

    public List<Notification> getAllNotification(int userId) {
        return notificationRepository.findAllByRecipientId(userId);
    }





//
//    public List<Notification> findNotificationsByRecipientIdOrderByCreatedAtDesc(int recipientId) {
//        return entityManager.createQuery(
//                        "SELECT n FROM Notification n WHERE n.recipient.id = :recipientId ORDER BY n.createdAt DESC", Notification.class)
//                .setParameter("recipientId", recipientId)
//                .getResultList();
//    }

}