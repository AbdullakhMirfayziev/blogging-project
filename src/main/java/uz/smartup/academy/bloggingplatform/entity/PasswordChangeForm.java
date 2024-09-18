package uz.smartup.academy.bloggingplatform.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PasswordChangeForm {
    private String oldPassword;

    private String newPassword;

    private String confirmPassword;
}
