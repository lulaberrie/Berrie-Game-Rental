package com.berrie.gamerental.model;

import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.TextScore;

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
    @TextIndexed
    @Field(value = "title")
    private String title;
    @TextScore
    private Float textScore;
    private Genre genre;
    private Platform platform;
    private GameStatus status;
    private Integer numberOfRentals;
    private User submittedBy;
}
