package com.berrie.gamerental.repository;

import com.berrie.gamerental.model.Rental;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Rental access object.
 */
public interface RentalRepository extends MongoRepository<Rental, String> { }
