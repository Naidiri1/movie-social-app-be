package com.iridian.movie.social.util;

import java.util.HashSet;
import java.util.Set;

import org.springframework.stereotype.Component;

@Component
public class TokenBlackList {

    private final Set<String> blacklist = new HashSet<>();

    public void add(String token) {
        blacklist.add(token);
    }

    public boolean isBlacklisted(String token) {
        return blacklist.contains(token);
    }
}
