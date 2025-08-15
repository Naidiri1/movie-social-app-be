package com.iridian.movie.social.util;

import java.util.List;
import java.util.Map;
import java.util.Objects;

// GenreMap.java
public final class GenreMap {

    private GenreMap() {
    }
    private static final Map<Integer, String> MAP = Map.ofEntries(
            Map.entry(28, "Action"), Map.entry(12, "Adventure"), Map.entry(16, "Animation"),
            Map.entry(35, "Comedy"), Map.entry(80, "Crime"), Map.entry(99, "Documentary"),
            Map.entry(18, "Drama"), Map.entry(10751, "Family"), Map.entry(14, "Fantasy"),
            Map.entry(36, "History"), Map.entry(27, "Horror"), Map.entry(10402, "Music"),
            Map.entry(9648, "Mystery"), Map.entry(10749, "Romance"),
            Map.entry(878, "Science Fiction"), Map.entry(10770, "TV Movie"),
            Map.entry(53, "Thriller"), Map.entry(10752, "War"), Map.entry(37, "Western")
    );

    public static List<String> toNames(List<Integer> ids) {
        if (ids == null) {
            return List.of();
        }
        return ids.stream().map(MAP::get).filter(Objects::nonNull).toList();
    }
}
