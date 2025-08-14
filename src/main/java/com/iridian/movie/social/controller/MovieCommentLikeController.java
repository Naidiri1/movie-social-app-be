package com.iridian.movie.social.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.iridian.movie.social.model.EntryType;
import com.iridian.movie.social.service.MovieCommentLikeService;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class MovieCommentLikeController {

    @Autowired(required = false)
    private MovieCommentLikeService likeService;

    @PostMapping("/{entryType}/{entryId}/like")
    public ResponseEntity<Map<String, Object>> likeEntry(
            @PathVariable String entryType,
            @PathVariable Long entryId,
            @RequestParam String userId) {

        try {
            EntryType type = convertToEntryType(entryType);

            if (likeService == null) {
                return mockLikeResponse(entryId, type, userId, true);
            }

            Map<String, Object> result = likeService.toggleLike(
                    entryId,
                    type.name(),
                    userId,
                    true
            );

            result.put("entryId", entryId);
            result.put("entryType", type.name());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid entry type: " + entryType));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/{entryType}/{entryId}/dislike")
    public ResponseEntity<Map<String, Object>> dislikeEntry(
            @PathVariable String entryType,
            @PathVariable Long entryId,
            @RequestParam String userId) {

        try {
            EntryType type = convertToEntryType(entryType);

            // If service is not available, return mock data
            if (likeService == null) {
                return mockLikeResponse(entryId, type, userId, false);
            }

            Map<String, Object> result = likeService.toggleLike(
                    entryId,
                    type.name(),
                    userId,
                    false
            );

            result.put("entryId", entryId);
            result.put("entryType", type.name());

            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid entry type: " + entryType));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{entryType}/{entryId}/likes")
    public ResponseEntity<Map<String, Object>> getEntryLikes(
            @PathVariable String entryType,
            @PathVariable Long entryId,
            @RequestParam(required = false) String userId) {

        try {
            EntryType type = convertToEntryType(entryType);

            if (likeService == null) {
                Map<String, Object> mockData = new HashMap<>();
                mockData.put("likes", 5L);
                mockData.put("dislikes", 2L);
                mockData.put("userStatus", userId != null ? "none" : null);
                mockData.put("entryId", entryId);
                mockData.put("entryType", type.name());
                mockData.put("message", "Mock response - service not available");
                return ResponseEntity.ok(mockData);
            }

            Map<String, Object> data = likeService.getLikeData(
                    entryId,
                    type,
                    userId
            );

            return ResponseEntity.ok(data);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid entry type: " + entryType));
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @DeleteMapping("/{entryType}/{entryId}/like")
    public ResponseEntity<Map<String, Object>> removeLike(
            @PathVariable String entryType,
            @PathVariable Long entryId,
            @RequestParam String userId) {

        try {
            EntryType type = convertToEntryType(entryType);

            if (likeService == null) {
                Map<String, Object> mockData = new HashMap<>();
                mockData.put("action", "removed");
                mockData.put("entryId", entryId);
                mockData.put("entryType", type.name());
                mockData.put("message", "Mock response - service not available");
                return ResponseEntity.ok(mockData);
            }

            Map<String, Object> result = likeService.removeLike(entryId, type, userId);
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     *
     * @param action
     */
    @PostMapping("/{entryType}/{entryId}/react")
    public ResponseEntity<Map<String, Object>> reactToEntry(
            @PathVariable String entryType,
            @PathVariable Long entryId,
            @RequestParam String action,
            @RequestParam String userId) {

        try {
            EntryType type = convertToEntryType(entryType);

            if (likeService == null) {
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .body(Map.of("error", "Like service not available"));
            }

            Map<String, Object> result;

            switch (action.toLowerCase()) {
                case "like":
                    result = likeService.toggleLike(entryId, type.name(), userId, true);
                    break;
                case "dislike":
                    result = likeService.toggleLike(entryId, type.name(), userId, false);
                    break;
                case "remove":
                    result = likeService.removeLike(entryId, type, userId);
                    break;
                default:
                    return ResponseEntity.badRequest()
                            .body(Map.of("error", "Invalid action. Use 'like', 'dislike', or 'remove'"));
            }

            result.put("requestedAction", action);
            result.put("entryType", type.name());
            return ResponseEntity.ok(result);

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    private EntryType convertToEntryType(String entryType) {
        switch (entryType.toLowerCase()) {
            case "favorites":
            case "favorite":
                return EntryType.FAVORITE;
            case "watched":
                return EntryType.WATCHED;
            case "top10":
                return EntryType.TOP10;
            case "watch-later":
            case "watchlater":
                return EntryType.WATCH_LATER;
            default:
                throw new IllegalArgumentException("Unknown entry type: " + entryType);
        }
    }

    private ResponseEntity<Map<String, Object>> mockLikeResponse(
            Long entryId, EntryType type, String userId, boolean isLike) {

        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("action", "added");
        mockResponse.put("likes", isLike ? 1L : 0L);
        mockResponse.put("dislikes", isLike ? 0L : 1L);
        mockResponse.put("entryId", entryId);
        mockResponse.put("entryType", type.name());
        mockResponse.put("userId", userId);
        mockResponse.put("message", "Mock response - service not available");

        return ResponseEntity.ok(mockResponse);
    }
}
