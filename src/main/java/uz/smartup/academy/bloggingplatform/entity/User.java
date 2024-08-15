package uz.smartup.academy.bloggingplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;

import java.util.List;
import java.util.Set;

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

//    public void setEmail(String email) {
//        int alpha = email.indexOf("@");
//        if(alpha == -1) throw new RuntimeException("Email is invalid!");
//        int dot = email.indexOf(".");
//        if(dot == -1) throw new RuntimeException("Email is invalid!");
//        String e1 = email.substring(0, alpha);
//        String e2 = email.substring(alpha + 1, dot - alpha - 1);
//        String e3 = email.substring(dot + 1);
//        if(e3.length() != 2 && e3.length() != 3) throw new RuntimeException("Email is invalid!");
//        for(int i = 0; i < e3.length(); ++i){
//            char c = e3.charAt(i);
//            if(!Character.isLetter(c)) throw new RuntimeException();
//        }
//
//        for(int i = 0; i < e2.length(); ++i){
//            char c = e2.charAt(i);
//            if(!Character.isLetter(c)) throw new RuntimeException();
//        }
//
//        for(int i = 0; i < e1.length(); ++i){
//            char c = e1.charAt(i);
//            if(Character.isLetter(c) || Character.isDigit(c)) continue;
//            if(c == '-' || c == '_' || c == '.') continue;
//            throw new RuntimeException();
//        }
//        this.email = email;
//
//
//    }
}
