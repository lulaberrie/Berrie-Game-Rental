package com.berrie.gamerental.dto;

import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.model.enums.RentalStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentalModel {

    private RentalStatus rentalStatus;
    private String gameTitle;
    private Genre gameGenre;
    private Platform gamePlatform;
    private String dateRented;
    private String dateReturned;
}
