package com.berrie.gamerental.repository;

import com.berrie.gamerental.model.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * User data access object.
 */
public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByUsername(String username);
}
