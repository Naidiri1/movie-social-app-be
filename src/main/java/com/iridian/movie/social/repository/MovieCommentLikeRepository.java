package com.iridian.movie.social.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.model.MovieCommentLike;

@Repository
public interface MovieCommentLikeRepository extends JpaRepository<MovieCommentLike, Long> {
    
    // ===== REMOVE THESE TWO - They cause the "isLike" error =====
    // List<MovieCommentLike> findByUserIdAndIsLike(String userId, Boolean isLike);
    // List<MovieCommentLike> findByMovieEntryIdAndEntryTypeAndIsLike(Long movieEntryId, EntryType entryType, Boolean isLike);
    // List<MovieCommentLike> findTop10ByIsLikeOrderByCreatedAtDesc(Boolean isLike);
    
    // ===== CORE METHODS (KEEP THESE) =====
    
    // 1. Find specific user's reaction on an entry
    Optional<MovieCommentLike> findByUserIdAndMovieEntryIdAndEntryType(
        String userId, Long movieEntryId, EntryType entryType);
    
    // 2. Count likes for an entry (using isLike in @Query is OK)
    @Query("SELECT COUNT(m) FROM MovieCommentLike m WHERE m.movieEntryId = :entryId " +
           "AND m.entryType = :entryType AND m.isLike = true")
    long countLikes(@Param("entryId") Long entryId, @Param("entryType") EntryType entryType);
    
    // 3. Count dislikes for an entry
    @Query("SELECT COUNT(m) FROM MovieCommentLike m WHERE m.movieEntryId = :entryId " +
           "AND m.entryType = :entryType AND m.isLike = false")
    long countDislikes(@Param("entryId") Long entryId, @Param("entryType") EntryType entryType);
    
    // 4. Get all likes for multiple entries (for batch operations)
    @Query("SELECT m FROM MovieCommentLike m WHERE m.movieEntryId IN :entryIds " +
           "AND m.entryType = :entryType")
    List<MovieCommentLike> findByMovieEntryIdsAndType(
        @Param("entryIds") List<Long> entryIds, 
        @Param("entryType") EntryType entryType);
    
    // 5. Get user's likes for multiple entries
    @Query("SELECT m FROM MovieCommentLike m WHERE m.userId = :userId " +
           "AND m.movieEntryId IN :entryIds AND m.entryType = :entryType")
    List<MovieCommentLike> findUserLikesForEntries(
        @Param("userId") String userId, 
        @Param("entryIds") List<Long> entryIds, 
        @Param("entryType") EntryType entryType);
    
    // ===== STATISTICS & ANALYTICS (KEEP THESE) =====
    
    // 6. Count total likes received by a user
    @Query("SELECT COUNT(m) FROM MovieCommentLike m WHERE m.movieOwnerId = :ownerId AND m.isLike = true")
    long countLikesForOwner(@Param("ownerId") String ownerId);
    
    // 7. Count total dislikes received by a user
    @Query("SELECT COUNT(m) FROM MovieCommentLike m WHERE m.movieOwnerId = :ownerId AND m.isLike = false")
    long countDislikesForOwner(@Param("ownerId") String ownerId);
    
    // 8. Get most liked entries (trending)
    @Query(value = "SELECT movie_entry_id, COUNT(*) as like_count " +
           "FROM movie_comment_likes " +
           "WHERE entry_type = :entryType AND is_like = true " +
           "GROUP BY movie_entry_id " +
           "ORDER BY like_count DESC " +
           "LIMIT :limit", nativeQuery = true)
    List<Object[]> findMostLikedEntries(@Param("entryType") String entryType, @Param("limit") int limit);
    
    // 9. Get all activity for a user's content
    @Query("SELECT m FROM MovieCommentLike m WHERE m.movieOwnerId = :ownerId ORDER BY m.createdAt DESC")
    List<MovieCommentLike> findByMovieOwnerId(@Param("ownerId") String ownerId);
    
    // 10. Get users who liked specific content
    @Query("SELECT m.userId FROM MovieCommentLike m WHERE m.movieEntryId = :entryId " +
           "AND m.entryType = :entryType AND m.isLike = true")
    List<String> findUserIdsWhoLiked(@Param("entryId") Long entryId, @Param("entryType") EntryType entryType);
    
    // ===== UTILITY METHODS (KEEP THESE) =====
    
    // 11. Delete all likes when entry is deleted
    void deleteByMovieEntryIdAndEntryType(Long movieEntryId, EntryType entryType);
    
    // 12. Check if user has reacted to entry
    boolean existsByUserIdAndMovieEntryIdAndEntryType(String userId, Long movieEntryId, EntryType entryType);
    
    // ===== ADVANCED ANALYTICS (KEEP THESE) =====
    
    // 13. Bulk check user's reactions
    @Query("SELECT m.movieEntryId, m.isLike FROM MovieCommentLike m " +
           "WHERE m.userId = :userId AND m.movieEntryId IN :entryIds AND m.entryType = :entryType")
    List<Object[]> findUserReactionsForEntries(@Param("userId") String userId, 
                                               @Param("entryIds") List<Long> entryIds,
                                               @Param("entryType") EntryType entryType);
    
    // 14. Get like statistics by type
    @Query("SELECT m.entryType, " +
           "COUNT(CASE WHEN m.isLike = true THEN 1 END) as likes, " +
           "COUNT(CASE WHEN m.isLike = false THEN 1 END) as dislikes " +
           "FROM MovieCommentLike m WHERE m.movieOwnerId = :ownerId " +
           "GROUP BY m.entryType")
    List<Object[]> getUserLikeStatsByType(@Param("ownerId") String ownerId);
    
    // 15. Find users with similar taste
    @Query("SELECT m2.userId, COUNT(m2.movieEntryId) as commonLikes " +
           "FROM MovieCommentLike m1 " +
           "JOIN MovieCommentLike m2 ON m1.movieEntryId = m2.movieEntryId " +
           "AND m1.entryType = m2.entryType " +
           "WHERE m1.userId = :userId1 AND m2.userId != :userId1 " +
           "AND m1.isLike = true AND m2.isLike = true " +
           "GROUP BY m2.userId " +
           "ORDER BY commonLikes DESC " +
           "LIMIT :limit")
    List<Object[]> findUsersWithSimilarTaste(@Param("userId1") String userId1, @Param("limit") int limit);
    
    // ===== REPLACEMENT METHODS FOR THE PROBLEMATIC ONES =====
    
    // Replace findByUserIdAndIsLike with this:
    @Query("SELECT m FROM MovieCommentLike m WHERE m.userId = :userId AND m.isLike = :isLike")
    List<MovieCommentLike> findByUserIdAndLikeStatus(@Param("userId") String userId, @Param("isLike") Boolean isLike);
    
    // Replace findTop10ByIsLikeOrderByCreatedAtDesc with this:
    @Query("SELECT m FROM MovieCommentLike m WHERE m.isLike = :isLike ORDER BY m.createdAt DESC LIMIT 10")
    List<MovieCommentLike> findRecentLikes(@Param("isLike") Boolean isLike);
}