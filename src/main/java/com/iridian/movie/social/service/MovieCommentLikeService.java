package com.iridian.movie.social.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.iridian.movie.social.dto.MovieEntryDTO;
import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.model.MovieCommentLike;
import com.iridian.movie.social.repository.FavoriteRepository;
import com.iridian.movie.social.repository.MovieCommentLikeRepository;
import com.iridian.movie.social.repository.Top10Repository;
import com.iridian.movie.social.repository.WatchLaterRepository;
import com.iridian.movie.social.repository.WatchedRepository;

@Service
@Transactional
public class MovieCommentLikeService {

    @Autowired
    private MovieCommentLikeRepository likeRepository;

    @Autowired
    private FavoriteRepository favoriteRepository;

    @Autowired(required = false)
    private WatchedRepository watchedRepository;

    @Autowired(required = false)
    private Top10Repository top10Repository;

    @Autowired(required = false)
    private WatchLaterRepository watchLaterRepository;

    /**
     * Toggle like/dislike on a movie comment
     */
    // In MovieCommentLikeService.java, update the toggleLike method:
    /**
     * Toggle like/dislike on a movie comment
     */
    public Map<Long, Map<String, Object>> getBatchLikeData(List<Long> entryIds,
            EntryType entryType,
            String viewerId) {
        Map<Long, Map<String, Object>> result = new HashMap<>();

        if (entryIds.isEmpty()) {
            return result;
        }

        // Initialize all entries with default values
        for (Long entryId : entryIds) {
            Map<String, Object> data = new HashMap<>();
            data.put("likes", 0L);
            data.put("dislikes", 0L);
            data.put("userStatus", null);
            result.put(entryId, data);
        }

        // Get all likes/dislikes for these entries
        List<MovieCommentLike> allLikes = likeRepository
                .findByMovieEntryIdsAndType(entryIds, entryType);

        // Count likes and dislikes for each entry
        for (MovieCommentLike like : allLikes) {
            Map<String, Object> entryData = result.get(like.getMovieEntryId());
            if (like.getIsLike()) {
                entryData.put("likes", ((Long) entryData.get("likes")) + 1);
            } else {
                entryData.put("dislikes", ((Long) entryData.get("dislikes")) + 1);
            }
        }

        // Get viewer's specific reactions if viewerId provided
        if (viewerId != null && !viewerId.isEmpty()) {
            List<MovieCommentLike> userLikes = likeRepository
                    .findUserLikesForEntries(viewerId, entryIds, entryType);

            for (MovieCommentLike userLike : userLikes) {
                Map<String, Object> entryData = result.get(userLike.getMovieEntryId());
                entryData.put("userStatus", userLike.getIsLike() ? "liked" : "disliked");
            }
        }

        return result;
    }

    public Map<String, Object> toggleLike(Long movieEntryId, String entryTypeStr,
            String currentUserId, boolean isLike) {

        EntryType entryType = EntryType.valueOf(entryTypeStr.toUpperCase());

        // Get the owner of the movie entry (who posted the favorite/watched/etc)
        String movieOwnerId = getEntryOwner(movieEntryId, entryType);
        if (movieOwnerId == null) {
            throw new RuntimeException("Movie entry not found with ID: " + movieEntryId + " and type: " + entryType);
        }

        // Users cannot like their own comments/entries
        if (movieOwnerId.equals(currentUserId)) {
            throw new RuntimeException("You cannot like your own " + entryType.toString().toLowerCase());
        }

        // Check for existing reaction
        Optional<MovieCommentLike> existing = likeRepository
                .findByUserIdAndMovieEntryIdAndEntryType(currentUserId, movieEntryId, entryType);

        Map<String, Object> response = new HashMap<>();

        if (existing.isPresent()) {
            MovieCommentLike reaction = existing.get();

            // Toggle off if clicking same reaction
            if (reaction.getIsLike() == isLike) {
                likeRepository.delete(reaction);
                response.put("action", "removed");
            } // Switch reaction (from like to dislike or vice versa)
            else {
                reaction.setIsLike(isLike);
                likeRepository.save(reaction);
                response.put("action", "switched");
            }
        } // Add new reaction
        else {
            MovieCommentLike newLike = new MovieCommentLike(
                    currentUserId, // who is liking
                    movieEntryId, // what entry they're liking
                    entryType, // type of entry (FAVORITE, WATCHED, etc)
                    movieOwnerId, // who owns the entry (THIS IS THE KEY PART)
                    isLike // like or dislike
            );
            likeRepository.save(newLike);
            response.put("action", "added");
        }

        // Return updated counts
        long likes = likeRepository.countLikes(movieEntryId, entryType);
        long dislikes = likeRepository.countDislikes(movieEntryId, entryType);
        response.put("likes", likes);
        response.put("dislikes", dislikes);
        response.put("entryId", movieEntryId);
        response.put("entryType", entryType.toString());

        return response;
    }

    /**
     * Get like counts and user status for a single entry
     */
    public Map<String, Object> getLikeData(Long entryId, EntryType entryType, String currentUserId) {
        Map<String, Object> data = new HashMap<>();

        // Get counts
        long likes = likeRepository.countLikes(entryId, entryType);
        long dislikes = likeRepository.countDislikes(entryId, entryType);

        data.put("likes", likes);
        data.put("dislikes", dislikes);
        data.put("entryId", entryId);
        data.put("entryType", entryType.toString());

        // Get current user's status if they're logged in
        if (currentUserId != null && !currentUserId.isEmpty()) {
            Optional<MovieCommentLike> userLike = likeRepository
                    .findByUserIdAndMovieEntryIdAndEntryType(currentUserId, entryId, entryType);

            if (userLike.isPresent()) {
                data.put("userStatus", userLike.get().getIsLike() ? "liked" : "disliked");
            } else {
                data.put("userStatus", null);
            }
        } else {
            data.put("userStatus", null);
        }

        return data;
    }

    /**
     * Helper method to get the owner of a movie entry This looks up who created
     * the favorite/watched/etc entry
     */
    private String getEntryOwner(Long entryId, EntryType type) {
        try {
            switch (type) {
                case FAVORITE:
                    return favoriteRepository.findById(entryId)
                            .map(f -> f.getUser().getUserId())
                            .orElse(null);

                case WATCHED:
                    if (watchedRepository != null) {
                        return watchedRepository.findById(entryId)
                                .map(w -> w.getUser().getUserId())
                                .orElse(null);
                    }
                    break;

                case TOP10:
                    if (top10Repository != null) {
                        return top10Repository.findById(entryId)
                                .map(t -> t.getUser().getUserId())
                                .orElse(null);
                    }
                    break;

                case WATCH_LATER:
                    if (watchLaterRepository != null) {
                        return watchLaterRepository.findById(entryId)
                                .map(wl -> wl.getUser().getUserId())
                                .orElse(null);
                    }
                    break;
            }
        } catch (Exception e) {
            System.err.println("Error getting entry owner: " + e.getMessage());
        }
        return null;
    }

    /**
     * Get user statistics (total likes/dislikes received)
     */
    public Map<String, Object> getUserStats(String userId) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalLikesReceived", likeRepository.countLikesForOwner(userId));
        stats.put("totalDislikesReceived", likeRepository.countDislikesForOwner(userId));
        stats.put("likeRatio", calculateLikeRatio(
                (Long) stats.get("totalLikesReceived"),
                (Long) stats.get("totalDislikesReceived")
        ));
        return stats;
    }

    /**
     * Get trending content by entry type
     */
    public List<Map<String, Object>> getTrendingContent(String entryType, int limit) {
        List<Object[]> results = likeRepository.findMostLikedEntries(entryType, limit);
        List<Map<String, Object>> trending = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> item = new HashMap<>();
            item.put("entryId", row[0]);
            item.put("likeCount", row[1]);
            item.put("entryType", entryType);

            // Fetch additional details based on entry type
            Long entryId = ((Number) row[0]).longValue();
            enrichWithEntryDetails(item, entryId, EntryType.valueOf(entryType));

            trending.add(item);
        }

        return trending;
    }

    /**
     * Get users who liked a specific entry
     */
    public List<String> getUsersWhoLiked(Long entryId, EntryType entryType) {
        return likeRepository.findUserIdsWhoLiked(entryId, entryType);
    }

    /**
     * Delete all likes when an entry is deleted
     */
    @Transactional
    public void removeAllLikesForEntry(Long entryId, EntryType entryType) {
        likeRepository.deleteByMovieEntryIdAndEntryType(entryId, entryType);
    }

    /**
     * Get recent activity for a user's content
     */
    public List<Map<String, Object>> getUserContentActivity(String userId, int limit) {
        List<MovieCommentLike> recentLikes = likeRepository.findByMovieOwnerId(userId);

        return recentLikes.stream()
                .limit(limit)
                .map(like -> {
                    Map<String, Object> activity = new HashMap<>();
                    activity.put("userId", like.getUserId());
                    activity.put("action", like.getIsLike() ? "liked" : "disliked");
                    activity.put("entryId", like.getMovieEntryId());
                    activity.put("entryType", like.getEntryType());
                    activity.put("timestamp", like.getCreatedAt());
                    return activity;
                })
                .collect(Collectors.toList());
    }

    /**
     * Find users with similar taste (who liked the same content)
     */
    public List<Map<String, Object>> findSimilarUsers(String userId, int limit) {
        List<Object[]> results = likeRepository.findUsersWithSimilarTaste(userId, limit);
        List<Map<String, Object>> similarUsers = new ArrayList<>();

        for (Object[] row : results) {
            Map<String, Object> user = new HashMap<>();
            user.put("userId", row[0]);
            user.put("commonLikes", row[1]);
            // You could fetch user details here
            similarUsers.add(user);
        }

        return similarUsers;
    }

    /**
     * Batch check if user has liked multiple entries (efficient for lists)
     */
    public Map<Long, String> getUserReactionsForMultipleEntries(String userId, List<Long> entryIds, EntryType entryType) {
        List<Object[]> results = likeRepository.findUserReactionsForEntries(userId, entryIds, entryType);
        Map<Long, String> reactions = new HashMap<>();

        for (Object[] row : results) {
            Long entryId = (Long) row[0];
            Boolean isLike = (Boolean) row[1];
            reactions.put(entryId, isLike ? "liked" : "disliked");
        }

        return reactions;
    }

// Helper methods
    private double calculateLikeRatio(long likes, long dislikes) {
        if (likes + dislikes == 0) {
            return 0.0;
        }
        return (double) likes / (likes + dislikes) * 100;
    }

    private void enrichWithEntryDetails(Map<String, Object> item, Long entryId, EntryType entryType) {
        switch (entryType) {
            case FAVORITE:
                favoriteRepository.findById(entryId).ifPresent(fav -> {
                    item.put("movieTitle", fav.getTitle());
                    item.put("movieId", fav.getMovieId());
                    item.put("posterPath", fav.getPosterPath());
                    item.put("owner", fav.getUser().getUsername());
                });
                break;
            case WATCHED:
                if (watchedRepository != null) {
                    watchedRepository.findById(entryId).ifPresent(watched -> {
                        item.put("movieTitle", watched.getTitle());
                        item.put("movieId", watched.getMovieId());
                        item.put("owner", watched.getUser().getUsername());
                    });
                }
                break;
            // Add other entry types as needed
        }
    }

    public void enrichDTOsWithLikes(List<? extends MovieEntryDTO> dtos,
            EntryType entryType,
            String currentUserId) {
        if (dtos.isEmpty()) {
            return;
        }

        // Extract all entry IDs
        List<Long> entryIds = dtos.stream()
                .map(MovieEntryDTO::getId)
                .collect(Collectors.toList());

        // Get batch like data
        Map<Long, Map<String, Object>> likesData = getBatchLikeData(
                entryIds,
                entryType,
                currentUserId
        );

        // Apply to DTOs
        for (MovieEntryDTO dto : dtos) {
            Map<String, Object> likeData = likesData.get(dto.getId());
            if (likeData != null) {
                dto.setCommentLikes((Long) likeData.get("likes"));
                dto.setCommentDislikes((Long) likeData.get("dislikes"));
                dto.setUserLikeStatus((String) likeData.get("userStatus"));
            }
        }
    }
}
