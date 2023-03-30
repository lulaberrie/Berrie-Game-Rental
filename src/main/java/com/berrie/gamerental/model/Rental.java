package com.berrie.gamerental.model;

import com.berrie.gamerental.model.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.DBRef;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Represents a rental record object.
 */
@Data
@Builder
@AllArgsConstructor
@Document(collection = "rentals")
public class Rental {
    @Id
    private String id;
    private RentalStatus rentalStatus;
    @DBRef
    private User user;
    @DBRef
    private Game game;
    private Date rentalDate;
    private Date returnDate;
    private String rentedBy;
}
