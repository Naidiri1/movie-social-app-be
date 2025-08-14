package com.iridian.movie.social.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.service.MovieCommentLikeService;

@RestController
@RequestMapping("/api/stats")
public class StatsController {
    
    @Autowired
    private MovieCommentLikeService likeService;
    
    /**
     * Get user's like statistics
     * GET /api/stats/user/{userId}
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<Map<String, Object>> getUserStats(@PathVariable String userId) {
        return ResponseEntity.ok(likeService.getUserStats(userId));
    }
    
    /**
     * Get trending content
     * GET /api/stats/trending?type=FAVORITE&limit=10
     */
    @GetMapping("/trending")
    public ResponseEntity<List<Map<String, Object>>> getTrending(
            @RequestParam(defaultValue = "FAVORITE") String type,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(likeService.getTrendingContent(type, limit));
    }
    
    /**
     * Get users who liked specific content
     * GET /api/stats/likes/{entryId}/users?type=FAVORITE
     */
    @GetMapping("/likes/{entryId}/users")
    public ResponseEntity<List<String>> getUsersWhoLiked(
            @PathVariable Long entryId,
            @RequestParam String type) {
        EntryType entryType = EntryType.valueOf(type.toUpperCase());
        return ResponseEntity.ok(likeService.getUsersWhoLiked(entryId, entryType));
    }
    
    /**
     * Get recent activity on user's content
     * GET /api/stats/user/{userId}/activity
     */
    @GetMapping("/user/{userId}/activity")
    public ResponseEntity<List<Map<String, Object>>> getUserActivity(
            @PathVariable String userId,
            @RequestParam(defaultValue = "20") int limit) {
        return ResponseEntity.ok(likeService.getUserContentActivity(userId, limit));
    }
    
    /**
     * Find users with similar taste
     * GET /api/stats/user/{userId}/similar
     */
    @GetMapping("/user/{userId}/similar")
    public ResponseEntity<List<Map<String, Object>>> getSimilarUsers(
            @PathVariable String userId,
            @RequestParam(defaultValue = "5") int limit) {
        return ResponseEntity.ok(likeService.findSimilarUsers(userId, limit));
    }
}