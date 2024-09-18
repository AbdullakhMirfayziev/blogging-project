package uz.smartup.academy.bloggingplatform.repository;

import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import uz.smartup.academy.bloggingplatform.entity.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    PasswordResetToken findByUser(uz.smartup.academy.bloggingplatform.entity.User user);

    PasswordResetToken findByToken(String token);
}


