package uz.smartup.academy.bloggingplatform.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import uz.smartup.academy.bloggingplatform.dto.UserDTO;
import uz.smartup.academy.bloggingplatform.entity.Notification;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByRecipientIdAndReadFalse(int recipientId);

    List<Notification> findAllByRecipientId(int recipient_id);

//    List<Notification> finfAllByRecipenetsId(int recipient_id);
}