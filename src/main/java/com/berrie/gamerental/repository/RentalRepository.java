package com.berrie.gamerental.repository;

import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.RentalStatus;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Rental access object.
 */
public interface RentalRepository extends MongoRepository<Rental, String> {
    List<Rental> findByUserAndRentalStatusOrderByRentalDateDesc(User user, RentalStatus rentalStatus);
}
