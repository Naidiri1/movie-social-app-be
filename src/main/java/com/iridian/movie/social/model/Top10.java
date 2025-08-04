package com.iridian.movie.social.model;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "top10")
public class Top10 implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Reference to the user
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "movie_id", nullable = false)
    private Long movieId;

    @Column(name = "title",  nullable = false,  length = 500)
     private String title;

   @Column(name = "poster_path", length = 500)
    private String posterPath;

     @Column(name = "comment")
    private String comment;

    @Column(name = "user_score")
    private Double userScore;

     @Column(name = "comment_enabled")
    private Boolean commentEnabled;


     @Column(name = "releasedDate")
    private String releasedDate;

    @Column(name = "movie_description", length = 1000)
    private String movieDescription;

    @Column(name = "public_score")
    private Double publicScore;

     @Column(name = "rank")
    private Integer rank;

    @Column(name = "created_at", nullable = false, updatable = false)
    private java.time.LocalDateTime createdAt = java.time.LocalDateTime.now();

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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

    public Boolean getCommentEnabled() {
    return commentEnabled;
    }

     public Double getPublicScore() {
        return publicScore;
    }

     public void setPublicScore(Double publicScore) {
        this.publicScore = publicScore;
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

    public void setRank(Integer rank) {
        this.rank = rank;
    }

    public Integer getRank() {
        return rank;
    }
     public void setMovieDescription(String movieDescription) {
        this.movieDescription = movieDescription;
    }

     public java.time.LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(java.time.LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
