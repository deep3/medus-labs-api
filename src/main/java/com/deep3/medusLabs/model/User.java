package com.deep3.medusLabs.model;

import com.deep3.medusLabs.utilities.PasswordComplexityConstraint;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import javax.persistence.*;
import javax.validation.Validation;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "TBL_USERS")

public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    private String username;

    @NotNull
    @PasswordComplexityConstraint
    private String password;

    private Date lastLoggedIn;

    public User() {}

    public User(Long id, String username, String password) {
        this.setId(id);
        this.setUsername(username);
        this.setPassword(password);
    }

    public User(String username, String password) {
        this.setUsername(username);
        this.password = password;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;

        if (Validation.buildDefaultValidatorFactory().getValidator().validate(this).isEmpty()) {
            this.password = passwordEncoder().encode(password);
        }
    }

    public void setAlreadyHashedPassword(String password) {
        this.password = password;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    public Date getLastLoggedIn() {
        return lastLoggedIn;
    }

    public void setLastLoggedIn(Date lastLoggedIn) {
        this.lastLoggedIn = lastLoggedIn;
    }
}
