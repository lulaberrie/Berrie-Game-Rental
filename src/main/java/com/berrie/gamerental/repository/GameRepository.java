package com.berrie.gamerental.repository;

import com.berrie.gamerental.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Game data access object.
 */
public interface GameRepository extends MongoRepository<Game, String> {
    List<Game> findAllByOrderByNumberOfRentalsDesc();
    List<Game> findAllByOrderByTitleAsc();
}
