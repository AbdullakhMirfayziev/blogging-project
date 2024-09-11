package uz.smartup.academy.bloggingplatform.mvc;

import jakarta.mail.MessagingException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.smartup.academy.bloggingplatform.dto.UserDTO;
import uz.smartup.academy.bloggingplatform.dto.UserDtoUtil;
import uz.smartup.academy.bloggingplatform.entity.NotificationTypes;
import uz.smartup.academy.bloggingplatform.entity.PasswordChangeForm;
import uz.smartup.academy.bloggingplatform.entity.PasswordResetToken;
import uz.smartup.academy.bloggingplatform.entity.User;
import uz.smartup.academy.bloggingplatform.repository.PasswordResetTokenRepository;
import uz.smartup.academy.bloggingplatform.service.CategoryService;
import uz.smartup.academy.bloggingplatform.service.MailSenderService;
import uz.smartup.academy.bloggingplatform.service.NotificationService;
import uz.smartup.academy.bloggingplatform.service.UserService;

import java.io.IOException;

@Controller
@RequestMapping
public class UrlController {

    private final UserService userService;
    private final MailSenderService mailSenderService;
    private final UserDtoUtil userDtoUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final CategoryService categoryService;
    private final NotificationService notificationService;


    public UrlController(UserService userService, MailSenderService mailSenderService, UserDtoUtil userDtoUtil, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder, CategoryService categoryService, NotificationService notificationService) {
        this.userService = userService;
        this.mailSenderService = mailSenderService;
        this.userDtoUtil = userDtoUtil;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.categoryService = categoryService;
        this.notificationService = notificationService;
    }

    @GetMapping("/password/reset")
    public String resetPassword(Model model) {
        model.addAttribute("userDTO", new UserDTO());
        return "password";
    }

    @PostMapping("/password/reset")
    public String processForgotPasswordForm(@ModelAttribute UserDTO userDTO, RedirectAttributes redirectAttributes) {
        UserDTO userDTO1 = userService.getUserByEmail(userDTO.getEmail());

        if (userDTO1 != null) {
            User user = userDtoUtil.toEntity(userDTO1);
            String output = mailSenderService.sendMail(user);
            if ("success".equals(output)) {
                redirectAttributes.addFlashAttribute("success", "Password reset email sent successfully!");
                return "redirect:/password/reset?success";
            } else {
                redirectAttributes.addFlashAttribute("error", "Error sending password reset email!");
                return "redirect:/password/reset?error";
            }
        } else {
            redirectAttributes.addFlashAttribute("errorValue", "No user found with this email address!");
            return "redirect:/password/reset?error";
        }
    }


    @GetMapping("/password/reset/{token}")
    public String resetPasswordToken(@PathVariable String token, Model model, RedirectAttributes redirectAttributes) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
        if (resetToken != null && !mailSenderService.hasExpired(resetToken.getExpiryDate())) {
            model.addAttribute("email", resetToken.getUser().getEmail());
            model.addAttribute("token", token);
            return "passwordReset";
        }
        redirectAttributes.addFlashAttribute("error", "Invalid or expired token!");
        return "redirect:/password/reset?error";
    }

    @PostMapping("/password/reset/{token}")
    public String passwordResetProcess(@ModelAttribute("passwordChangeForm") PasswordChangeForm form,
                                       Model model,
                                       RedirectAttributes attributes,
                                       @PathVariable String token,
                                       HttpServletRequest request) throws ServletException {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token);
//        System.out.println(resetToken.getExpiryDate());
        if (mailSenderService.hasExpired(resetToken.getExpiryDate())) {
            attributes.addFlashAttribute("error", "Invalid or expired token!");
            return "redirect:/password/reset?error";
        }

        User user = resetToken.getUser();
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            model.addAttribute("error", "New password and confirm password do not match.");
            model.addAttribute("token", token);
            return "passwordReset";
        }

        String rawPassword = form.getNewPassword();

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        notificationService.addNotification(user.getId(), user.getId(), 0, "your password changed successfully!", "/notifications", NotificationTypes.P);
        userService.updateUser(userDtoUtil.toDTO(user));


        attributes.addFlashAttribute("success", "Password changed successfully.");

        if (getLoggedUser() == null)
            request.login(user.getUsername(), rawPassword);

        return "redirect:/";
    }

    @GetMapping("/vacancy")
    public String vacancy(Model model) {

        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : userService.getUserByUsername(getLoggedUser().getUsername());
        if (userDTO != null) {
            photo = userService.encodePhotoToBase64(userDTO.getPhoto());
        }


        model.addAttribute("photo", photo);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("loggedIn", getLoggedUser());

        return "vacancy";
    }


    @PostMapping("/sendEmail")
    public String sendEmail(@RequestParam("cv") MultipartFile cv) {
        try {
            UserDTO userDTO = userService.getUserByUsername(getLoggedUser().getUsername());

            String subject = "Job Application: Post Editor";
            String text = "Username: " + userDTO.getUsername() + "\n\nEmail: " + userDTO.getEmail() + "\n\nAttached is the CV.";
            mailSenderService.sendEmailWithAttachment(
                    "greenwhitenews@gmail.com",
                    subject,
                    text,
                    cv.getOriginalFilename(),
                    new ByteArrayResource(cv.getBytes())
            );

            return "redirect:/vacancy?success";
        } catch (MessagingException | IOException e) {
            e.printStackTrace();
            return "redirect:/vacancy?error";
        }
    }

    private UserDetails getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails)
            return (UserDetails) principal;

        return null;
    }


}