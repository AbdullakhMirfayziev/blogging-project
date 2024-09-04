package uz.smartup.academy.bloggingplatform.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.transaction.Transactional;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import uz.smartup.academy.bloggingplatform.entity.PasswordResetToken;
import uz.smartup.academy.bloggingplatform.entity.User;
import uz.smartup.academy.bloggingplatform.exceptions.EmailSendingException;

@Service
public class EmailVerificationService {

    private final MailSenderService mailSenderService;
    private final TemplateEngine templateEngine;
    private final JavaMailSender javaMailSender;

    public EmailVerificationService(MailSenderService mailSenderService, TemplateEngine templateEngine, JavaMailSender javaMailSender) {
        this.mailSenderService = mailSenderService;
        this.templateEngine = templateEngine;
        this.javaMailSender = javaMailSender;
    }


    @Transactional
    public void sendHtmlMail(User user) throws MessagingException {
        try {
            PasswordResetToken verificationToken = mailSenderService.findByUser(user);

            if (verificationToken != null) {
                String token = verificationToken.getToken();
                Context context = new Context();
                context.setVariable("title", "Verify your Email!");
//                context.setVariable("link", ServletUriComponentsBuilder.fromCurrentContextPath().path("email-confirmation?token=").toUriString() + token);
                context.setVariable("link", "http://localhost:8080/email-confirmation?token=" + token);

                String body = templateEngine.process("emailConfirmation", context);

                MimeMessage mimeMessage = javaMailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true);
                helper.setTo(user.getEmail());
                helper.setSubject("Email address verification");
                helper.setText(body, true);
                javaMailSender.send(mimeMessage);
            }

        } catch (MessagingException e) {
            throw new EmailSendingException("Failed to send email to " + user.getEmail(), e);
        }
    }

}
