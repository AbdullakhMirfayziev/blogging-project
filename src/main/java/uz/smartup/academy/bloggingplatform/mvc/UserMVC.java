package uz.smartup.academy.bloggingplatform.mvc;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StreamUtils;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import uz.smartup.academy.bloggingplatform.dto.UserDTO;
import uz.smartup.academy.bloggingplatform.dto.UserDtoUtil;
import uz.smartup.academy.bloggingplatform.entity.PasswordChangeForm;
import uz.smartup.academy.bloggingplatform.entity.PasswordResetToken;
import uz.smartup.academy.bloggingplatform.entity.Role;
import uz.smartup.academy.bloggingplatform.entity.User;
import uz.smartup.academy.bloggingplatform.service.MailSenderService;
import uz.smartup.academy.bloggingplatform.service.UserService;


import java.io.IOException;
import java.security.Principal;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.validation.FieldError;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Validated
@Controller
public class UserMVC {

    @Value("classpath:static/css/photos/deleted_user.jpg")
    private Resource defaultPhotoResource;

    private byte[] defaultPhoto;

    private final UserService service;
    private final PasswordEncoder passwordEncoder;
    private final MailSenderService mailSenderService;
    private final UserDtoUtil userDtoUtil;
    private static final Logger logger = LoggerFactory.getLogger(UserMVC.class);


    public UserMVC(UserService userService, PasswordEncoder passwordEncoder, MailSenderService mailSenderService, UserDtoUtil userDtoUtil) {
        this.service = userService;
        this.passwordEncoder = passwordEncoder;
        this.mailSenderService = mailSenderService;
        this.userDtoUtil = userDtoUtil;
    }

    @PostConstruct
    public void init() throws IOException {
        defaultPhoto = StreamUtils.copyToByteArray(defaultPhotoResource.getInputStream());
    }

    @GetMapping("/register")
    public String createUser(Model model) {
        model.addAttribute("user", new UserDTO());

        return "register";
    }

    @PostMapping("/register-user")
    public ResponseEntity<?> createUser(@Valid @ModelAttribute("user") UserDTO user, HttpServletRequest request, HttpSession session, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = bindingResult.getFieldErrors().stream()
                    .collect(Collectors.toMap(
                            FieldError::getField,
                            FieldError::getDefaultMessage
                    ));
            return ResponseEntity.badRequest().body(errors);
        }
        
        
        if (request.getHeader("X-Requested-With") != null && request.getHeader("X-Requested-With").equals("XMLHttpRequest")) {
            if(service.userExists(user.getUsername(), user.getEmail())) {
                return ResponseEntity.badRequest().body(Collections.singletonMap("error", "Username or email is already taken"));
            }

            List<Role> roles = new ArrayList<>();
            Role role = new Role();
            role.setRole("ROLE_VIEWER");
            role.setUsername(user.getUsername());
            roles.add(role);
            service.registerUserWithConfirmation(user, roles);

            session.setAttribute("registrationMessage", "Registration successful. Check your email inbox and verify your email.");
            session.setAttribute("user", user);
            return ResponseEntity.ok(Collections.singletonMap("redirect", "/login"));

        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonMap("error", "Invalid request"));
    }


    @GetMapping("/auto-login")
    public String autoLogin(HttpSession session, HttpServletRequest request) {
        UserDTO userDTO = (UserDTO) session.getAttribute("user");

        if (userDTO != null) {
            session.removeAttribute("user");
            try {
                request.login(userDTO.getUsername(), userDTO.getPassword());
                return "redirect:/";
            } catch (ServletException e) {
                return "redirect:/login?error=true";
            }
        }

        return "redirect:/login";
    }




//    @PostMapping("/register-user")
//    public String createUser(Model model, @ModelAttribute("user") UserDTO user, RedirectAttributes attributes, HttpServletRequest request) {
//
//        if(service.userExists(user.getUsername(), user.getEmail())) {
//
//            System.out.println("-".repeat(100));
//            System.out.println("user already exists");
//            System.out.println("-".repeat(100));
//
//            model.addAttribute("error", "this username or email is already taken");
//            model.addAttribute("user", new UserDTO());
//            return "register";
//        }
//
//        List<Role> roles = new ArrayList<>();
//        Role role = new Role();
//        role.setRole("ROLE_VIEWER");
//        role.setUsername(user.getUsername());
//        roles.add(role);
//        service.registerUserWithConfirmation(user, roles);
//
//        attributes.addFlashAttribute("success", "Registration successful! A verification email has been sent.");
//        return "redirect:/login";
//    }



    @GetMapping("/email-confirmation")
    public String activation(@RequestParam("token") String token, Model model){
        PasswordResetToken verificationToken = mailSenderService.findByToken(token);
        if(verificationToken == null){
            model.addAttribute("message", "Your verification token is invalid!");

        }
        else{
            User user = verificationToken.getUser();
            if(user.getEnabled().equals("0")){
                if(verificationToken.isExpired()){
                    model.addAttribute("message", "Token is expired!");
                }
                else{
                    user.setEnabled("1");
                    UserDTO userDTO = userDtoUtil.toDTO(user);
                    service.updateUser(userDTO);
                    model.addAttribute("message", "Account is successfully activated!");
                }
            }
            else {
                model.addAttribute("message", "Your account is already activated!");
            }
        }
        return "activation";
    }


    @GetMapping("/changePassword")
    public String showChangePasswordForm(Model model) {
        String photo = "";
        UserDTO userDTO = getLoggedUser() == null ? null : service.getUserByUsername(getLoggedUser().getUsername());
        if (userDTO != null) {
            photo = service.encodePhotoToBase64(userDTO.getPhoto());
        }

        model.addAttribute("passwordChangeForm", new PasswordChangeForm());
        model.addAttribute("photo", photo);
        model.addAttribute("loggedIn", getLoggedUser());
        return "changePassword";
    }

    @PostMapping("/password-change")
    public String changePassword(@Valid @ModelAttribute("passwordChangeForm") PasswordChangeForm form, Model model, Principal principal, RedirectAttributes attributes, BindingResult bindingResult) {
        UserDTO loggedUser = service.getUserByUsername(principal.getName());
        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "changePassword";
        }

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            String photo = "";
            UserDTO userDTO = getLoggedUser() == null ? null : service.getUserByUsername(getLoggedUser().getUsername());
            if (userDTO != null) {
                photo = service.encodePhotoToBase64(userDTO.getPhoto());
            }
            model.addAttribute("error", "New password and confirm password do not match.");
            model.addAttribute("passwordChangeForm", new PasswordChangeForm());
            model.addAttribute("photo", photo);
            model.addAttribute("loggedIn", getLoggedUser());
            return "changePassword";
        }

        if (!passwordEncoder.matches(form.getOldPassword(), loggedUser.getPassword())) {
            String photo = "";
            UserDTO userDTO = getLoggedUser() == null ? null : service.getUserByUsername(getLoggedUser().getUsername());
            if (userDTO != null) {
                photo = service.encodePhotoToBase64(userDTO.getPhoto());
            }
            model.addAttribute("error", "Old password is incorrect.");
            model.addAttribute("passwordChangeForm", new PasswordChangeForm());
            model.addAttribute("photo", photo);
            model.addAttribute("loggedIn", getLoggedUser());
            return "changePassword";
        }

        service.changePassword(loggedUser.getUsername(), form.getNewPassword());
        attributes.addFlashAttribute("success", "Password changed successfully.");
        return "redirect:/";
    }

    @GetMapping("/login")
    public String login(Model model, HttpSession session) {
        String message = (String) session.getAttribute("registrationMessage");
        if (message != null) {
            model.addAttribute("message", message);
            session.removeAttribute("registrationMessage");
        }
        return "login";
    }

    private UserDetails getLoggedUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (principal instanceof UserDetails)
            return (UserDetails) principal;

        return null;
    }

    @GetMapping("/complete-registration")
    public String showCompleteRegistrationPage(Model model, HttpSession session) {
        if (!model.containsAttribute("userDTO")) {
            model.addAttribute("userDTO", new UserDTO());
        }

        OAuth2User oauth2User = (OAuth2User) session.getAttribute("oauth2User");
        model.addAttribute("email", oauth2User.getAttribute("email"));
        return "setPassword";
    }

    @PostMapping("/complete-registration")
    public String completeRegistration(@Valid @ModelAttribute("userDTO") UserDTO userDTO,
                                       @RequestParam String email,
                                       @RequestParam String username,
                                       @RequestParam String password,
                                       @RequestParam String confirmPassword,
                                       Model model,
                                       HttpSession session,
                                       BindingResult bindingResult,
                                       HttpServletRequest request) throws ServletException {

        if (bindingResult.hasErrors()) {
            model.addAttribute("errors", bindingResult.getAllErrors());
            return "setPassword";
        }

        if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "Passwords do not match");
            return "setPassword";
        }

        OAuth2User oauth2User = (OAuth2User) session.getAttribute("oauth2User");
        String firstName = oauth2User.getAttribute("given_name");
        String lastName = oauth2User.getAttribute("family_name");

        List<Role> roles = new ArrayList<>();
        Role role = new Role();
        role.setRole("ROLE_VIEWER");
        role.setUsername(username);
        roles.add(role);

        userDTO.setUsername(username);
        userDTO.setFirst_name(firstName);
        userDTO.setLast_name(lastName);
        userDTO.setPassword(password);
        userDTO.setEmail(email);

        try {
            service.registerUser(userDTO, roles);
        } catch (Exception e) {
            model.addAttribute("error", "Error registering user: " + e.getMessage());
            return "setPassword";
        }

        Authentication auth = new UsernamePasswordAuthenticationToken(username, service.getUserByUsername(username).getPassword());
        SecurityContextHolder.getContext().setAuthentication(auth);

        try {
            request.login(username, password);
        } catch (ServletException e) {
            model.addAttribute("error", "Error logging in: " + e.getMessage());
            return "setPassword";
        }

        return "redirect:/";
    }

    @PostMapping("/users/{username}/delete")
    public String deleteUser(@PathVariable String username, HttpSession session, RedirectAttributes redirectAttributes) {
        try {
            UserDTO userDTO = service.getUserByUsername(username);
            String loggedInUsername = (String) session.getAttribute("username");
            String newUsername = "deleted_user_" + userDTO.getId();
            userDTO.setUsername(newUsername);
            userDTO.setEnabled("0");
            userDTO.setEmail(newUsername + "@gmail.com");
//            userDTO.setFirst_name("Deleted");
//            userDTO.setLast_name("User");
            userDTO.setBio(null);
            userDTO.setPhoto(defaultPhoto);
            userDTO.setRegistered(null);

            service.removeRolesFromUser(userDTO.getId());
            service.updateUser(userDTO);


            session.invalidate();
            redirectAttributes.addFlashAttribute("message", "Your account has been successfully deleted.");
            return "redirect:/login";
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Error deleting user: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "An error occurred while deleting the profile: " + e.getMessage());
            return "redirect:/profile/" + username;
        }
    }




}
