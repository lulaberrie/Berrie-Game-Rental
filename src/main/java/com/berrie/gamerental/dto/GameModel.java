package com.berrie.gamerental.dto;

import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GameModel {

    private String title;
    private Genre genre;
    private Platform platform;
    private GameStatus status;
    private Integer numberOfRentals;
    private String submittedBy;
}
