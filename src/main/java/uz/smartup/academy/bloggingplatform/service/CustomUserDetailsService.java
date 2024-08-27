package uz.smartup.academy.bloggingplatform.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        String userQuery = "SELECT username, password, enabled FROM user WHERE username = ? OR email = ?";

        List<UserDetails> users = jdbcTemplate.query(userQuery, new Object[]{usernameOrEmail, usernameOrEmail}, userRowMapper());

        if (users.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
        }

        return users.get(0);
    }

    private RowMapper<UserDetails> userRowMapper() {
        return (rs, rowNum) -> {
            String username = rs.getString("username");
            String password = rs.getString("password");
            boolean enabled = rs.getBoolean("enabled");

            // Query to retrieve roles/authorities for the user
            String roleQuery = "SELECT role AS authority FROM role WHERE username = ?";
            List<SimpleGrantedAuthority> authorities = jdbcTemplate.query(roleQuery, new Object[]{username},
                    (rs1, rowNum1) -> new SimpleGrantedAuthority(rs1.getString("authority")));

            return new User(username, password, enabled, true, true, true, authorities);
        };
    }
}

