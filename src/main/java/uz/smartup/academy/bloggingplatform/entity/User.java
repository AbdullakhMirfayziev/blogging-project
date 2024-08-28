package uz.smartup.academy.bloggingplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.*;

@Getter
@Entity
@Table(name = "user")
@Setter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Lob
    @Column(name = "photo", columnDefinition = "LONGBLOB")
    private byte[] photo;

    @Column(name = "username", length = 50, unique = true)
    private String username;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "bio", length = 400)
    private String bio;

    @Column(name = "registered")
    private LocalDate registered;

    @Column(name = "enabled")
    private String enabled ;

    @Column(name = "password", length = 100)
    private String password;

    @Column(name = "web_push_token")
    private String webPushToken;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "user_follows",
            joinColumns = @JoinColumn(name = "follower_id"),
            inverseJoinColumns = @JoinColumn(name = "followed_id")
    )
    private Set<User> following;

    @ManyToMany(mappedBy = "following", fetch = FetchType.LAZY)
    private Set<User> followers;

    @OneToMany(  cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "username", referencedColumnName = "username", updatable = false)
    private List<Role> roles;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Post> posts;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments;

    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Like> likes;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PasswordResetToken> passwordResetTokens;

    public void addFollower(User follower) {
        followers.add(follower);
    }

    public void removeFollower(User follower) {
        followers.remove(follower);
    }

    public void follow(User userToFollow) {
        following.add(userToFollow);
    }

    public void unfollow(User userToUnfollow) {
        following.remove(userToUnfollow);
    }

    public void addPostToAuthor(Post post){
        if(posts == null) posts = new ArrayList<>();
        posts.add(post);
        post.setAuthor(this);
    }
    public  void removeAuthorsPost(Post post){
        if(posts != null) posts.remove(post);
        post.setAuthor(null);
    }

    public void addRole(Role role) {
        if(roles.isEmpty()) {
            roles = new ArrayList<>();
        }

        roles.add(role);
    }

    public void removeRole(Role role) {
        if(!roles.isEmpty())
            roles.remove(role);
    }
}
