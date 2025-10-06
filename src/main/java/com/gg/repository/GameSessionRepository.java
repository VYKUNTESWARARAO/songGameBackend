package com.gg.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.gg.model.GameSession;

@Repository
public interface GameSessionRepository extends MongoRepository<GameSession, String> {
    Optional<GameSession> findByPlayerIdAndIsActive(String playerId, boolean isActive);
}
