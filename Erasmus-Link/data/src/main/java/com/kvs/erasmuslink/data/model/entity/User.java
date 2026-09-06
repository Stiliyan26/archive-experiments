package com.kvs.erasmuslink.data.model.entity;

import com.kvs.erasmuslink.data.model.enums.UserType;
import jakarta.persistence.*;

import java.time.LocalDateTime;

/**
 * Database Entity for User
 *
 * @author Venislav Kirilov
 */
@Entity
@Table(name = "erasmus_user")
public class User {
    @Id
    @GeneratedValue(strategy= GenerationType.AUTO)
    private Integer id;

    private String fullName;
    private String username;
    private String email;

    @Enumerated(EnumType.STRING)
    private UserType userType;

    private String passwordHash;

    private LocalDateTime createdAt;
    private LocalDateTime lastLogin;

//    @Column(columnDefinition = "jsonb")
//    private String settings;

    public User() {}

    public User(String fullName, String username, String email, UserType userType, String passwordHash) {
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.userType = userType;
        this.passwordHash = passwordHash;
        this.createdAt = LocalDateTime.now();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getFullName() { return fullName; }

    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(LocalDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

//    public String getSettings() {
//        return settings;
//    }
//
//    public void setSettings(String settings) {
//        this.settings = settings;
//    }
}
