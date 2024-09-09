package uz.smartup.academy.bloggingplatform.entity;


import jakarta.persistence.Embeddable;

@Embeddable
public class RoleKey {

    private String username;
    private String role;

    public RoleKey(String username, String role) {
        this.username = username;
        this.role = role;
    }

    public RoleKey() {
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @Override
    public String toString() {
        return "RoleKey{" +
                "username='" + username + '\'' +
                ", role='" + role + '\'' +
                '}';
    }

}
