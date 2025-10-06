package com.gg.model;

import java.time.LocalDateTime;

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
    private int currentIndex; // current song index
    private int score; // score accumulated in this session
    private LocalDateTime startTime; // for timing each guess
    private boolean isActive; // session active or finished
}
