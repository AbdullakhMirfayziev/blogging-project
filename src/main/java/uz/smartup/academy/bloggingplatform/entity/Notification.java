package uz.smartup.academy.bloggingplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class Notification {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @ManyToOne
    @JoinColumn(name = "recipient_id")
    private User recipient;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "post_id")
    private int postId;

    @Column(name = "message")
    private String message;

    @Column(name = "`read`")
    private Boolean read;

    @Column(name = "redirect_url")
    private String redirectUrl;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private NotificationTypes type;

    @Column(name = "notified")
    private Boolean notify;

    @Override
    public String toString() {
        return "Notification{" +
                "id=" + id +
                ", recipient=" + recipient +
                ", message='" + message + '\'' +
                ", read=" + read +
                ", redirectUrl='" + redirectUrl + '\'' +
                ", createdAt=" + createdAt +
                ", type='" + type + '\'' +
                '}';
    }
}
