package uz.smartup.academy.bloggingplatform.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "role")
public class Role {

    @EmbeddedId
    private RoleKey id;

    public RoleKey getId() {
        return id;
    }

    public void setId(RoleKey id) {
        this.id = id;
    }

    public String getUsername() {
        return id.getUsername();
    }

    public void setUsername(String username) {
        if (this.id == null) {
            this.id = new RoleKey();
        }
        this.id.setUsername(username);
    }

    public String getRole() {
        return id.getRole();
    }

    public void setRole(String role) {
        if (this.id == null) {
            this.id = new RoleKey();
        }
        this.id.setRole(role);
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                '}';
    }
}
