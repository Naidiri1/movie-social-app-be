package com.iridian.movie.social.dto;

public abstract class MovieEntryDTO {

    private Long id;
    private Long commentLikes;
    private Long commentDislikes;
    private String userLikeStatus;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCommentLikes() {
        return commentLikes;
    }

    public void setCommentLikes(Long commentLikes) {
        this.commentLikes = commentLikes;
    }

    public Long getCommentDislikes() {
        return commentDislikes;
    }

    public void setCommentDislikes(Long commentDislikes) {
        this.commentDislikes = commentDislikes;
    }

    public String getUserLikeStatus() {
        return userLikeStatus;
    }

    public void setUserLikeStatus(String userLikeStatus) {
        this.userLikeStatus = userLikeStatus;
    }
}
