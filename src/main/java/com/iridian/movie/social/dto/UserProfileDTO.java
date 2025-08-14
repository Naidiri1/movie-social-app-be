package com.iridian.movie.social.dto;

import java.util.List;

/**
 * DTO for complete user profile data
 * Email excluded for security
 */
public class UserProfileDTO {
    private String userId;
    private String username;
    private List<FavoriteFlat> favorites;
    private List<WatchedFlat> watched;
    private List<Top10Flat> top10;
    private List<WatchLaterFlat> watchLater;
    
    // Statistics
    private int favoritesCount;
    private int watchedCount;
    private int top10Count;
    private int watchLaterCount;
    
    public UserProfileDTO(String userId, String username,
                         List<FavoriteFlat> favorites, List<WatchedFlat> watched,
                         List<Top10Flat> top10, List<WatchLaterFlat> watchLater) {
        this.userId = userId;
        this.username = username;
        this.favorites = favorites;
        this.watched = watched;
        this.top10 = top10;
        this.watchLater = watchLater;
        
        // Calculate counts
        this.favoritesCount = favorites != null ? favorites.size() : 0;
        this.watchedCount = watched != null ? watched.size() : 0;
        this.top10Count = top10 != null ? top10.size() : 0;
        this.watchLaterCount = watchLater != null ? watchLater.size() : 0;
    }
    
    // Getters and Setters
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
    
    
    public List<FavoriteFlat> getFavorites() {
        return favorites;
    }
    
    public void setFavorites(List<FavoriteFlat> favorites) {
        this.favorites = favorites;
        this.favoritesCount = favorites != null ? favorites.size() : 0;
    }
    
    public List<WatchedFlat> getWatched() {
        return watched;
    }
    
    public void setWatched(List<WatchedFlat> watched) {
        this.watched = watched;
        this.watchedCount = watched != null ? watched.size() : 0;
    }
    
    public List<Top10Flat> getTop10() {
        return top10;
    }
    
    public void setTop10(List<Top10Flat> top10) {
        this.top10 = top10;
        this.top10Count = top10 != null ? top10.size() : 0;
    }
    
    public List<WatchLaterFlat> getWatchLater() {
        return watchLater;
    }
    
    public void setWatchLater(List<WatchLaterFlat> watchLater) {
        this.watchLater = watchLater;
        this.watchLaterCount = watchLater != null ? watchLater.size() : 0;
    }
    
    public int getFavoritesCount() {
        return favoritesCount;
    }
    
    public int getWatchedCount() {
        return watchedCount;
    }
    
    public int getTop10Count() {
        return top10Count;
    }
    
    public int getWatchLaterCount() {
        return watchLaterCount;
    }
}