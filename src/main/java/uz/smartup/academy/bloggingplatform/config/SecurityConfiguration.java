package uz.smartup.academy.bloggingplatform.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.firewall.DefaultHttpFirewall;
import org.springframework.security.web.firewall.HttpFirewall;


import javax.sql.DataSource;

@Configuration
public class SecurityConfiguration {

    @Autowired
    private OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager detailsManager = new JdbcUserDetailsManager(dataSource);

        detailsManager.setUsersByUsernameQuery("SELECT username, password, enabled FROM user WHERE username = ?");
        detailsManager.setAuthoritiesByUsernameQuery("SELECT username, role FROM role WHERE username = ?");

        return detailsManager;
    }

//    @Bean
//    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
//        return authenticationConfiguration.getAuthenticationManager();
//    }

//    @Bean
//    public UserDetailsService userDetailsService() {
//        return new CustomUserDetailsService();
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authManager ->
                        authManager
                                .requestMatchers(HttpMethod.GET, "/?page=*", "/?page=**","/?size=**", "/?keyword=**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/admin", "/admin/**").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.POST, "/admin", "/admin/**").hasAnyRole("ADMIN")
                                .requestMatchers(HttpMethod.GET, "/editor", "/editor/**").hasAnyRole("EDITOR")
                                .requestMatchers(HttpMethod.POST, "/editor", "/editor/**").hasAnyRole("EDITOR")
                                .requestMatchers(HttpMethod.GET, "/password/reset", "/auto-login","/password/reset/*", "/login?success=true").permitAll()
                                .requestMatchers(HttpMethod.POST, "/password/reset", "/password/reset/*").permitAll()
                                .requestMatchers(HttpMethod.GET,  "/", "/register", "/register/*", "/posts/*", "/profile/*", "/categories/*", "/profile", "/search", "/posts/tags/*", "/posts/author/*").permitAll()
                                .requestMatchers(HttpMethod.POST, "/register-user").permitAll()
                                .requestMatchers(HttpMethod.GET, "/css/**", "/js/**", "/photos/**").permitAll()
                                .requestMatchers(HttpMethod.GET, "/changePassword").authenticated()
                                .requestMatchers(HttpMethod.POST, "/password-change").authenticated()
                                .requestMatchers(HttpMethod.GET, "/password/reset", "/password/reset/*", "/email-confirmation").permitAll()
                                .requestMatchers(HttpMethod.POST, "/password/reset", "/password/reset/*").permitAll()
                                .requestMatchers(HttpMethod.GET, "/changePassword").authenticated()
                                .requestMatchers(HttpMethod.POST, "/password-change", "/profile/*").authenticated()
                                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/authenticate")
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .successHandler(oAuth2LoginSuccessHandler)
                        .permitAll()
                )
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .accessDeniedPage("/access-denied")
                );

        http.csrf(AbstractHttpConfigurer::disable);
        http.httpBasic(Customizer.withDefaults());

        return http.build();
    }

}
