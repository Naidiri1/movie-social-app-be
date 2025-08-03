package com.iridian.movie.social.dto;

import java.time.LocalDateTime;

public class Top10Flat {
    private Long id;
    private String userId;
    private String username;

    private Long movieId;
    private String title;
    private String posterPath;
    private String comment;
    private Double userScore;
    private Boolean commentEnabled;
    private String releasedDate;
    private String movieDescription;
    private Double publicScore;
    private LocalDateTime createdAt;

    public Top10Flat(Long id, String userId, String username,
                            Long movieId, String title, String posterPath, String comment,
                            Double userScore, Boolean commentEnabled, String releasedDate,
                            String movieDescription, Double publicScore, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.username = username;
        this.movieId = movieId;
        this.title = title;
        this.posterPath = posterPath;
        this.comment = comment;
        this.userScore = userScore;
        this.commentEnabled = commentEnabled;
        this.releasedDate = releasedDate;
        this.movieDescription = movieDescription;
        this.publicScore = publicScore;
        this.createdAt = createdAt;
    }


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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }


    public Long getMovieId() {
        return movieId;
    }

    public void setMovieId(Long movieId) {
        this.movieId = movieId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Double getUserScore() {
        return userScore;
    }

    public void setUserScore(Double userScore) {
        this.userScore = userScore;
    }

     public Double getPublicScore() {
        return publicScore;
    }

    public void setPublicScore(Double publicScore) {
        this.publicScore = publicScore;
    }

    public Boolean getCommentEnabled() {
        return commentEnabled;
    }

    public void setCommentEnabled(Boolean commentEnabled) {
        this.commentEnabled = commentEnabled;
    }

    public String getReleasedDate() {
        return releasedDate;
    }

    public void setReleasedDate(String releasedDate) {
        this.releasedDate = releasedDate;
    }

    public String getMovieDescription() {
        return movieDescription;
    }

    public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
