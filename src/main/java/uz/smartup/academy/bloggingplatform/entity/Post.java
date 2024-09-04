package uz.smartup.academy.bloggingplatform.entity;

import jakarta.persistence.*;
import java.sql.Timestamp;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "post")
@Getter
@Setter
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Column(name = "title", length = 255)
    private String title;

    @Lob
    @Column(name = "photo", columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "created_at", nullable = false, insertable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    @ManyToOne(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinColumn(name = "user_id")
    private User author;

//    @Column(name = "new_notification")
//    private Boolean notification;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL)
    private List<Comment> comments;

    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH})
    @JoinTable(
        name = "category_post",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private List<Category> categories;


    @ManyToMany(cascade = {CascadeType.DETACH, CascadeType.MERGE, CascadeType.PERSIST, CascadeType.REFRESH}, fetch = FetchType.EAGER)
    @JoinTable(
            name = "tag_post",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private List<Tag> tags;

    @Transient
    private long likesCount;

    @Transient
    private long commentsCount;

    @OneToOne(mappedBy = "post",cascade = CascadeType.ALL)
    private PostSchedule postSchedule;


    public void addCategories(Category category) {
        if(categories == null || categories.isEmpty())
            categories = new ArrayList<>();

        categories.add(category);
    }

    public void addComments(Comment comment) {
        if(comments.isEmpty())
            comments = new ArrayList<>();

        comments.add(comment);
        comment.setPost(this);
    }

    public void removeComment(Comment comment) {
        if(!comments.isEmpty())
            comments.remove(comment);
    }

    public void removeCategory(Category category) {
        if(!categories.isEmpty())
            categories.remove(category);
    }

    public void addTag(Tag tag) {
        if(tags == null || tags.isEmpty())
            tags = new ArrayList<>();

        tags.add(tag);
    }

    public void removeTag(Tag tag) {
        if(!tags.isEmpty() || tag != null)
            tags.remove(tag);

    }


    public enum Status {
        DRAFT,
        PUBLISHED
    }
}
