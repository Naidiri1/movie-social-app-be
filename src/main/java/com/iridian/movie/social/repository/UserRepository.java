package com.iridian.movie.social.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iridian.movie.social.dto.UserSearchDTO;
import com.iridian.movie.social.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
     Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByUserId(String userId); 
    Page<User> findByUsernameContainingIgnoreCase(String username, Pageable pageable);
    
    @Query("SELECT new com.iridian.movie.social.dto.UserSearchDTO(" +
           "u.userId, " +
           "u.username, " +
           "COALESCE((SELECT COUNT(f) FROM Favorites f WHERE f.user.userId = u.userId), 0), " +
           "COALESCE((SELECT COUNT(w) FROM Watched w WHERE w.user.userId = u.userId), 0), " +
           "COALESCE((SELECT COUNT(t) FROM Top10 t WHERE t.user.userId = u.userId), 0), " +
           "COALESCE((SELECT COUNT(wl) FROM WatchLater wl WHERE wl.user.userId = u.userId), 0)) " +
           "FROM User u " +
           "WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))")
    Page<UserSearchDTO> searchUsersWithCounts(@Param("query") String query, Pageable pageable);
    
    Optional<User> findByShareSlugAndShareEnabledTrue(UUID shareSlug);
    
    @Query("SELECT new com.iridian.movie.social.dto.UserSearchDTO(" +
           "u.userId, " +
           "u.username, " +
           "COALESCE((SELECT COUNT(f) FROM Favorites f WHERE f.user.userId = u.userId), 0), " +
           "COALESCE((SELECT COUNT(w) FROM Watched w WHERE w.user.userId = u.userId), 0), " +
           "COALESCE((SELECT COUNT(t) FROM Top10 t WHERE t.user.userId = u.userId), 0), " +
           "COALESCE((SELECT COUNT(wl) FROM WatchLater wl WHERE wl.user.userId = u.userId), 0)) " +
           "FROM User u " +
           "ORDER BY (" +
           "COALESCE((SELECT COUNT(f) FROM Favorites f WHERE f.user.userId = u.userId), 0) + " +
           "COALESCE((SELECT COUNT(w) FROM Watched w WHERE w.user.userId = u.userId), 0) + " +
           "COALESCE((SELECT COUNT(t) FROM Top10 t WHERE t.user.userId = u.userId), 0) + " +
           "COALESCE((SELECT COUNT(wl) FROM WatchLater wl WHERE wl.user.userId = u.userId), 0)) DESC")
    Page<UserSearchDTO> findMostActiveUsers(Pageable pageable);
}