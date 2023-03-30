package com.berrie.gamerental.repository;

import com.berrie.gamerental.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Game data access object.
 */
public interface GameRepository extends MongoRepository<Game, String> {
    List<Game> findAllByOrderByNumberOfRentalsDesc();
    List<Game> findAllByOrderByTitleAsc();
    Optional<Game> findGameById(String gameId);
}
