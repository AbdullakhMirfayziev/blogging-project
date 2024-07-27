package uz.smartup.academy.bloggingplatform.mvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.smartup.academy.bloggingplatform.dto.UserDTO;
import uz.smartup.academy.bloggingplatform.dto.UserDtoUtil;
import uz.smartup.academy.bloggingplatform.entity.PasswordChangeForm;
import uz.smartup.academy.bloggingplatform.entity.PasswordResetToken;
import uz.smartup.academy.bloggingplatform.entity.User;
import uz.smartup.academy.bloggingplatform.repository.PasswordResetTokenRepository;
import uz.smartup.academy.bloggingplatform.service.MailSenderService;
import uz.smartup.academy.bloggingplatform.service.UserService;
@Controller
@RequestMapping
public class UrlController {

    private final UserService userService;
    private final MailSenderService mailSenderService;
    private final UserDtoUtil userDtoUtil;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder  passwordEncoder;

    public UrlController(UserService userService, MailSenderService mailSenderService, UserDtoUtil userDtoUtil, PasswordResetTokenRepository passwordResetTokenRepository, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.mailSenderService = mailSenderService;
        this.userDtoUtil = userDtoUtil;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
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
                                       @PathVariable String token) {
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

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userService.updateUser(userDtoUtil.toDTO(user)); // assuming updateUser saves the updated user

        attributes.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/";
    }







}