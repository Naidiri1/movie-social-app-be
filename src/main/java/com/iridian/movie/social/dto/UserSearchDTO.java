package com.iridian.movie.social.dto;

public class UserSearchDTO {

    private String userId;
    private String username;
    private long favoritesCount;
    private long watchedCount;
    private long top10Count;
    private long watchLaterCount;
    private long totalCount;

    public UserSearchDTO() {
    }

    public UserSearchDTO(String userId, String username,
            long favoritesCount, long watchedCount,
            long top10Count, long watchLaterCount) {
        this.userId = userId;
        this.username = username;
        this.favoritesCount = favoritesCount;
        this.watchedCount = watchedCount;
        this.top10Count = top10Count;
        this.watchLaterCount = watchLaterCount;
        this.totalCount = favoritesCount + watchedCount + top10Count + watchLaterCount;
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

    public long getFavoritesCount() {
        return favoritesCount;
    }

    public void setFavoritesCount(long favoritesCount) {
        this.favoritesCount = favoritesCount;
        updateTotalCount();
    }

    public long getWatchedCount() {
        return watchedCount;
    }

    public void setWatchedCount(long watchedCount) {
        this.watchedCount = watchedCount;
        updateTotalCount();
    }

    public long getTop10Count() {
        return top10Count;
    }

    public void setTop10Count(long top10Count) {
        this.top10Count = top10Count;
        updateTotalCount();
    }

    public long getWatchLaterCount() {
        return watchLaterCount;
    }

    public void setWatchLaterCount(long watchLaterCount) {
        this.watchLaterCount = watchLaterCount;
        updateTotalCount();
    }

    public long getTotalCount() {
        return totalCount;
    }

    private void updateTotalCount() {
        this.totalCount = favoritesCount + watchedCount + top10Count + watchLaterCount;
    }

    @Override
    public String toString() {
        return "UserSearchDTO{"
                + "userId='" + userId + '\''
                + ", username='" + username + '\''
                + ", favoritesCount=" + favoritesCount
                + ", watchedCount=" + watchedCount
                + ", top10Count=" + top10Count
                + ", watchLaterCount=" + watchLaterCount
                + ", totalCount=" + totalCount
                + '}';
    }
}
