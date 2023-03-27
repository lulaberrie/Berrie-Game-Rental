package com.berrie.gamerental.repository;

import com.berrie.gamerental.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Game data access object.
 */
public interface GameRepository extends MongoRepository<Game, String> {}
