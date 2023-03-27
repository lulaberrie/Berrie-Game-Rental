package com.berrie.gamerental.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * Represents a game object.
 */
@Data
@Builder
@AllArgsConstructor
@Document(collection = "games")
public class Game {
    @Id
    private String id;
    private String title;
    private Genre genre;
    private Platform platform;
    private GameStatus status;
    private Integer numberOfRentals;
    private User submittedBy;
}
