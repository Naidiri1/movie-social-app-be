package com.iridian.movie.social.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "movie_comment_likes",
    uniqueConstraints = @UniqueConstraint(
        name = "unique_user_movie_entry",
        columnNames = {"user_id", "movie_entry_id", "entry_type"}
    ))
public class MovieCommentLike {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false, length = 255)
    private String userId;
    
    @Column(name = "movie_entry_id", nullable = false)
    private Long movieEntryId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 50)
    private EntryType entryType;
    
    @Column(name = "movie_owner_id", nullable = false, length = 255)
    private String movieOwnerId;
    
    // THIS IS THE KEY: Use proper field name without 'is' prefix
    @Column(name = "is_like", nullable = false)
    private Boolean isLike;  // Keep this field name
    
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // Constructors
    public MovieCommentLike() {
    }
    
    public MovieCommentLike(String userId, Long movieEntryId, 
                           EntryType entryType, String movieOwnerId, Boolean isLike) {
        this.userId = userId;
        this.movieEntryId = movieEntryId;
        this.entryType = entryType;
        this.movieOwnerId = movieOwnerId;
        this.isLike = isLike;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public Long getMovieEntryId() {
        return movieEntryId;
    }
    
    public void setMovieEntryId(Long movieEntryId) {
        this.movieEntryId = movieEntryId;
    }
    
    public EntryType getEntryType() {
        return entryType;
    }
    
    public void setEntryType(EntryType entryType) {
        this.entryType = entryType;
    }
    
    public String getMovieOwnerId() {
        return movieOwnerId;
    }
    
    public void setMovieOwnerId(String movieOwnerId) {
        this.movieOwnerId = movieOwnerId;
    }
    
    // IMPORTANT: Proper getter/setter for boolean field
    // For boolean fields, use 'is' prefix in getter
    public Boolean isLike() {
        return isLike;
    }
    
    // Alternative getter (both will work)
    public Boolean getIsLike() {
        return isLike;
    }
    
    public void setIsLike(Boolean isLike) {
        this.isLike = isLike;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}