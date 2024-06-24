package uz.smartup.academy.bloggingplatform.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
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
    @Column(name = "photo")
    private byte[] photo;

    @Column(name = "username", length = 50)
    private String username;

    @Column(name = "first_name", length = 50)
    private String firstName;

    @Column(name = "last_name", length = 50)
    private String lastName;

    @Column(name = "email", length = 50)
    private String email;

    @Column(name = "bio", length = 400)
    private String bio;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "registered")
    private Date registered;

    @Column(name = "password", length = 50)
    private String password;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER, orphanRemoval = true)
    @JoinColumn(name = "username", referencedColumnName = "username", updatable = false)
    private Set<Role> roles;


}
