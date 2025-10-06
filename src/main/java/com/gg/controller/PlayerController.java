package com.gg.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.gg.model.GameSession;
import com.gg.model.Song;
import com.gg.service.GameService;

@RestController
@RequestMapping("/player")
@CrossOrigin(origins = "*")
public class PlayerController {

    @Autowired
    private GameService gameService;

    // 1️⃣ Start game
    @PostMapping("/start")
    public ResponseEntity<?> startGame(@RequestParam String playerId) {
        try {
            GameSession session = gameService.startGame(playerId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to start game", "details", e.getMessage()));
        }
    }

    // 2️⃣ Restart game
    @PostMapping("/restart")
    public ResponseEntity<?> restartGame(@RequestParam String playerId) {
        try {
            GameSession session = gameService.restartGame(playerId);
            return ResponseEntity.ok(session);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to restart game", "details", e.getMessage()));
        }
    }

    // 3️⃣ Get current song
    @GetMapping("/current-song")
    public ResponseEntity<?> getCurrentSong(@RequestParam String playerId) {
        try {
            Song song = gameService.getCurrentSong(playerId);
            if (song == null) {
                // Game over
                return ResponseEntity.ok(Map.of(
                        "gameOver", true,
                        "message", "Game completed! No more songs available.",
                        "score", gameService.getScore(playerId)
                ));
            }
            return ResponseEntity.ok(song);
        } catch (RuntimeException e) {
            // Could be session missing or other game errors
            return ResponseEntity.ok(Map.of(
                    "gameOver", true,
                    "message", e.getMessage(),
                    "score", gameService.getScore(playerId)
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error", "details", e.getMessage()));
        }
    }

    // 4️⃣ Submit guess or skip/timeout
    @PostMapping("/guess")
    public ResponseEntity<?> submitGuess(@RequestParam String playerId, @RequestParam String guess) {
        try {
            int score = gameService.submitGuess(playerId, guess);
            return ResponseEntity.ok(Map.of("score", score));
        } catch (RuntimeException e) {
            return ResponseEntity.ok(Map.of(
                    "score", 0,
                    "error", e.getMessage()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error", "details", e.getMessage()));
        }
    }

    // 5️⃣ Get score
    @GetMapping("/score")
    public ResponseEntity<?> getScore(@RequestParam String playerId) {
        try {
            int score = gameService.getScore(playerId);
            return ResponseEntity.ok(Map.of("score", score));
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error", "details", e.getMessage()));
        }
    }
}
