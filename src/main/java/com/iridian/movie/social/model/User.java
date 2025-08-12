package com.iridian.movie.social.model;

import java.io.Serializable;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "users")
public class User implements Serializable {

    @Id
    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    @Column(name = "username", nullable = false, unique = true)
    private String username;

    @Column(name = "email", nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "share_slug", columnDefinition = "UUID")
    private UUID shareSlug;

    @Column(name = "share_enabled", nullable = false)
    private Boolean shareEnabled = false;
    // === Getters and Setters ===

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public UUID getShareSlug() {
        return shareSlug;
    }

    public void setShareSlug(UUID shareSlug) {
        this.shareSlug = shareSlug;
    }

    public Boolean getShareEnabled() {
        return shareEnabled;
    }

    public void setShareEnabled(Boolean shareEnabled) {
        this.shareEnabled = shareEnabled;
    }

}
