package com.gg.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.gg.model.GameSession;
import com.gg.model.Song;
import com.gg.repository.GameSessionRepository;
import com.gg.repository.SongRepository;

@Service
public class GameService {

    @Autowired
    private GameSessionRepository gameSessionRepository;

    @Autowired
    private SongRepository songRepository;

    // 🔹 Start a new game or resume an existing active one
    public GameSession startGame(String playerId) {
        Optional<GameSession> existing = gameSessionRepository.findByPlayerIdAndIsActive(playerId, true);

        if (existing.isPresent()) {
            return existing.get(); // continue same session if already active
        }

        GameSession session = new GameSession();
        session.setPlayerId(playerId);
        session.setCurrentIndex(0);
        session.setScore(0);
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);

        return gameSessionRepository.save(session);
    }

    // 🔹 Restart the game — reset everything
    public GameSession restartGame(String playerId) {
        // Mark all old sessions inactive
        List<GameSession> oldSessions = gameSessionRepository.findAll();
        for (GameSession s : oldSessions) {
            if (s.getPlayerId().equals(playerId)) {
                s.setActive(false);
                gameSessionRepository.save(s);
            }
        }

        // Create new session
        GameSession newSession = new GameSession();
        newSession.setPlayerId(playerId);
        newSession.setCurrentIndex(0);
        newSession.setScore(0);
        newSession.setStartTime(LocalDateTime.now());
        newSession.setActive(true);

        return gameSessionRepository.save(newSession);
    }

    // 🔹 Get the current song for player
    public Song getCurrentSong(String playerId) {
        GameSession session = gameSessionRepository.findByPlayerIdAndIsActive(playerId, true)
                .orElseThrow(() -> new RuntimeException("No active session found for player"));

        List<Song> songs = songRepository.findAll();
        if (songs.isEmpty()) {
            throw new RuntimeException("No songs found in database");
        }

        // Use currentIndex or random index
        int index = session.getCurrentIndex();
        if (index >= songs.size()) {
            // End game if out of songs
            session.setActive(false);
            gameSessionRepository.save(session);
            throw new RuntimeException("Game completed! No more songs available.");
        }

        return songs.get(index);
    }

    // 🔹 Submit player guess, skip, or timeout and calculate score
    public int submitGuess(String playerId, String guess) {
        GameSession session = gameSessionRepository.findByPlayerIdAndIsActive(playerId, true)
                .orElseThrow(() -> new RuntimeException("No active session found for player"));

        List<Song> songs = songRepository.findAll();
        if (songs.isEmpty() || session.getCurrentIndex() >= songs.size()) {
            throw new RuntimeException("No song to guess");
        }

        Song currentSong = songs.get(session.getCurrentIndex());
        LocalDateTime now = LocalDateTime.now();

        // Calculate time taken
        long secondsTaken = Duration.between(session.getStartTime(), now).toSeconds();

        int points = 0;
        // Interpret "" or "SKIP" or "TIMEOUT" as a wrong guess intentionally (for timeout/time skip)
        if (guess == null || guess.trim().isEmpty() ||
                guess.equalsIgnoreCase("SKIP") ||
                guess.equalsIgnoreCase("TIMEOUT")) {
            // No points for skip/timeout
            points = 0;
        } else if (guess.equalsIgnoreCase(currentSong.getMovieName())) {
            if (secondsTaken <= 10) points = 10;
            else if (secondsTaken <= 20) points = 5;
            else if (secondsTaken <= 30) points = 3;
        } else {
            points = 0;
        }

        session.setScore(session.getScore() + points);
        session.setCurrentIndex(session.getCurrentIndex() + 1); // always advance
        session.setStartTime(LocalDateTime.now());

        gameSessionRepository.save(session);
        return points;
    }

    // 🔹 Get total score
    public int getScore(String playerId) {
        return gameSessionRepository.findByPlayerIdAndIsActive(playerId, true)
                .map(GameSession::getScore)
                .orElse(0);
    }
}