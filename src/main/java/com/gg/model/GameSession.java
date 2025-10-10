package com.gg.model;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "game_sessions")
public class GameSession {
    @Id
    private String id;
    private String playerId;
    private int score; // score accumulated in this session
    private LocalDateTime startTime; // for timing each guess
    private boolean isActive; // session active or finished

    private Set<String> playedSongIds = new HashSet<>(); // song ids played so far
    private int wrongGuesses = 0; // number of incorrect guesses so far
}
