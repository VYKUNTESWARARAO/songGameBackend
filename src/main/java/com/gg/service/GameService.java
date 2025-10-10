package com.gg.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
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

    // Start a new game or resume an existing active one
    public GameSession startGame(String playerId) {
        Optional<GameSession> existing = gameSessionRepository.findByPlayerIdAndIsActive(playerId, true);

        if (existing.isPresent()) {
            return existing.get();
        }

        GameSession session = new GameSession();
        session.setPlayerId(playerId);
        session.setScore(0);
        session.setStartTime(LocalDateTime.now());
        session.setActive(true);
        session.setPlayedSongIds(new HashSet<>());
        session.setWrongGuesses(0);

        return gameSessionRepository.save(session);
    }

    // Restart game — reset everything
    public GameSession restartGame(String playerId) {
        List<GameSession> oldSessions = gameSessionRepository.findAll();
        for (GameSession s : oldSessions) {
            if (s.getPlayerId().equals(playerId)) {
                s.setActive(false);
                gameSessionRepository.save(s);
            }
        }

        GameSession newSession = new GameSession();
        newSession.setPlayerId(playerId);
        newSession.setScore(0);
        newSession.setStartTime(LocalDateTime.now());
        newSession.setActive(true);
        newSession.setPlayedSongIds(new HashSet<>());
        newSession.setWrongGuesses(0);

        return gameSessionRepository.save(newSession);
    }

    // Get the current (next) song for player — only show unplayed!
    public Song getCurrentSong(String playerId) {
        GameSession session = gameSessionRepository.findByPlayerIdAndIsActive(playerId, true)
                .orElseThrow(() -> new RuntimeException("No active session found for player"));

        List<Song> songs = songRepository.findAll();
        if (songs.isEmpty()) {
            throw new RuntimeException("No songs found in database");
        }

        if (session.getWrongGuesses() >= 3) {
            session.setActive(false);
            gameSessionRepository.save(session);
            throw new RuntimeException("Game completed! Maximum incorrect guesses reached.");
        }

        // Find first unplayed song
        Song nextSong = null;
        for (Song s : songs) {
            if (!session.getPlayedSongIds().contains(s.getId())) {
                nextSong = s;
                break;
            }
        }

        if (nextSong == null) {
            // All songs played: end game
            session.setActive(false);
            gameSessionRepository.save(session);
            throw new RuntimeException("Game completed! No more songs available.");
        }

        return nextSong;
    }

    // Submit guess or timeout
    public int submitGuess(String playerId, String guess) {
        GameSession session = gameSessionRepository.findByPlayerIdAndIsActive(playerId, true)
                .orElseThrow(() -> new RuntimeException("No active session found for player"));

        List<Song> songs = songRepository.findAll();
        if (songs.isEmpty()) {
            throw new RuntimeException("No song to guess");
        }

        // Find a song the player hasn't played yet
        Song currentSong = null;
        for (Song s : songs) {
            if (!session.getPlayedSongIds().contains(s.getId())) {
                currentSong = s;
                break;
            }
        }

        if (currentSong == null) {
            throw new RuntimeException("No song to guess");
        }

        LocalDateTime now = LocalDateTime.now();
        long secondsTaken = Duration.between(session.getStartTime(), now).toSeconds();

        int points = 0;
        boolean correct = false;

        // Interpret ""/"SKIP"/"TIMEOUT" as a wrong guess
        if (guess == null || guess.trim().isEmpty() ||
                guess.equalsIgnoreCase("SKIP") ||
                guess.equalsIgnoreCase("TIMEOUT")) {
            // Incorrect guess, points stays 0
        } else if (guess.trim().equalsIgnoreCase(currentSong.getMovieName())) {
            if (secondsTaken <= 10) points = 10;
            else if (secondsTaken <= 20) points = 5;
            else if (secondsTaken <= 30) points = 3;
            correct = true;
        }

        session.setScore(session.getScore() + points);

        // Mark this song as played (IDs must be String)
        session.getPlayedSongIds().add(currentSong.getId());

        // Update wrong guesses only for incorrect guess
        if (!correct) {
            session.setWrongGuesses(session.getWrongGuesses() + 1);
        }

        session.setStartTime(LocalDateTime.now());
        gameSessionRepository.save(session);

        return points;
    }

    // Get total score
    public int getScore(String playerId) {
        return gameSessionRepository.findByPlayerIdAndIsActive(playerId, true)
                .map(GameSession::getScore)
                .orElse(0);
    }
}
