package uz.smartup.academy.bloggingplatform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.core.io.InputStreamSource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import uz.smartup.academy.bloggingplatform.dao.LikeDAO;
import uz.smartup.academy.bloggingplatform.dao.PostDao;
import uz.smartup.academy.bloggingplatform.entity.PasswordResetToken;
import uz.smartup.academy.bloggingplatform.entity.User;
import uz.smartup.academy.bloggingplatform.repository.PasswordResetTokenRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class MailSenderServiceImpl implements MailSenderService {

    private final JavaMailSender mailSender;
    //    private final UserService userService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private static final int EXPIRATION_TIME_IN_MINUTES = 60 * 24;


    public MailSenderServiceImpl(JavaMailSender mailSender, PasswordResetTokenRepository passwordResetTokenRepository, CategoryService categoryService, PostDao postDao, LikeDAO likeDAO) {
        this.mailSender = mailSender;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        //        this.postService = postService;
    }

    @Override
    public String sendMail(uz.smartup.academy.bloggingplatform.entity.User user) {
        try {
            PasswordResetToken existingToken = passwordResetTokenRepository.findByUser(user);
            if (existingToken != null) {
                passwordResetTokenRepository.delete(existingToken);
            }

            String resetLink = createPasswordResetTokenForUser(user);

            SimpleMailMessage simpleMailMessage = getSimpleMailMessage(user, resetLink);

            mailSender.send(simpleMailMessage);
            return "success";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }

    private static SimpleMailMessage getSimpleMailMessage(uz.smartup.academy.bloggingplatform.entity.User user, String resetLink) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setFrom("greenwhitenews@gmail.com");
        simpleMailMessage.setTo(user.getEmail());
        simpleMailMessage.setSubject("Reset Password Request");
        simpleMailMessage.setText("Please click on the link below to reset your password:\n\n" + resetLink + "\n\n" +
                "If you did not request this, please ignore this email.\n\n" +
                "Regards,\nGreen White News Support");
        return simpleMailMessage;
    }


    @Override
    @Transactional
    public String createPasswordResetTokenForUser(User user) {
        UUID uuid = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime expiryDate = now.plusMinutes(EXPIRATION_TIME_IN_MINUTES);

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setToken(uuid.toString());
        resetToken.setUser(user);
        resetToken.setExpiryDate(expiryDate);

        PasswordResetToken savedToken = passwordResetTokenRepository.save(resetToken);

        return generateResetLink(savedToken.getToken());
    }

    private String generateResetLink(String token) {
        String endpointUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/password/reset").toUriString();
//        String endpointUrl = "http://localhost:8080/password/reset";
        return endpointUrl + "/" + token;
    }

    private LocalDateTime calculateExpiryDate() {
        LocalDateTime now = LocalDateTime.now();
        return now.plusMinutes(EXPIRATION_TIME_IN_MINUTES);
    }

    @Override
    public boolean hasExpired(LocalDateTime expiryDate) {
        return LocalDateTime.now().isAfter(expiryDate);
    }


    @Override
    @Transactional
    public void save(User user, String token) {
        PasswordResetToken verificationToken = new PasswordResetToken(token, user);
        verificationToken.setExpiryDate(calculateExpiryDate());
        passwordResetTokenRepository.save(verificationToken);
    }

    @Override
    public PasswordResetToken findByUser(User user) {
        return passwordResetTokenRepository.findByUser(user);
    }

    @Override
    public PasswordResetToken findByToken(String token) {
        return passwordResetTokenRepository.findByToken(token);
    }

    @Override
    public void sendEmailWithAttachment(String to, String subject, String text, String fileName, InputStreamSource inputStreamSource) throws IOException, MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(text);
        helper.addAttachment(fileName, inputStreamSource);

        if (!to.contains("deleted_user")) mailSender.send(message);

    }

    @Override
    public void sendEmailNotification(User recipient, String message) {
        SimpleMailMessage simpleMailMessage = new SimpleMailMessage();
        simpleMailMessage.setTo(recipient.getEmail());
        simpleMailMessage.setSubject("New notification");
        simpleMailMessage.setText(message);

        if (!recipient.getUsername().contains("deleted_user")) mailSender.send(simpleMailMessage);
    }

    @Override
    public void sendPostLikedEmail(String toEmail, String username, int postId, String liker) {
//        String postUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/posts").toUriString();
        String postUrl = "http://localhost:8080/posts/" + postId;
        String subject = "Your Post was Liked!";
        String content = "<p>Hi, " + username + "!</p>"
                + liker + " liked your <a href='" + postUrl + "'>post</a>.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            if (!username.contains("deleted_user")) mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    public void sendPostCommentEmail(String toEmail, String username, int postId, String commenter) {
//        String postUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/posts/{id}").buildAndExpand(postId).toUriString();
        String postUrl = "http://localhost:8080/posts/" + postId;
        String subject = "Your Post was Commented!";
        String content = "<p>Hi, " + username + "!</p>"
                + commenter + " commented your <a href='" + postUrl + "'>post</a>.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            if (!username.contains("deleted_user")) mailSender.send(message);

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void sendPostNotificationToFollowers(String toEmail, String username, int postId, String author) {
//        String postUrl = ServletUriComponentsBuilder.fromCurrentContextPath().path("/posts/{id}").buildAndExpand(postId).toUriString();
        String postUrl = "http://localhost:8080/posts/author/" + author;
        String subject = "New Post";
        String content = "<p>Hi, " + username + "!</p>"
                + author + " added new <a href='" + postUrl + "'>post</a>.";

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(content, true);

            if (!toEmail.contains("deleted_user")) mailSender.send(message);

            System.out.println("-".repeat(100));
            System.out.println("message");
            System.out.println("-".repeat(100));

        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }


}


