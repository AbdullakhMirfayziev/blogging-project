package uz.smartup.academy.bloggingplatform.dto;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import uz.smartup.academy.bloggingplatform.entity.Post;
import uz.smartup.academy.bloggingplatform.entity.Role;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import uz.smartup.academy.bloggingplatform.service.LikeService;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class UserDTO {
    private int id;
    private String first_name;
    private String last_name;
    private byte[] photo;
    private String email;
    private String bio;
    private String enabled;
    @Size(min = 8, message = "Password must be at least 8 characters long")
    @Pattern(regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=.]).*$", message = "Password must contain a mix of characters")
    private String password;
    @Size(min = 4, message = "password must contain at least 4 characters long")
    @Pattern(regexp = "^[a-z0-9_]{4,25}$")
    private String username;
    private LocalDate registered;
    private List<Role> roles;
    private List<Post> authorsPosts;

}
