package com.github.mehrdadfalahati.redisutils.example.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Example User entity for demonstrating Redis caching.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {
    private String id;
    private String username;
    private String email;
    private LocalDateTime createdAt;
    private boolean active;

    public static User create(String id, String username, String email) {
        return new User(id, username, email, LocalDateTime.now(), true);
    }
}
