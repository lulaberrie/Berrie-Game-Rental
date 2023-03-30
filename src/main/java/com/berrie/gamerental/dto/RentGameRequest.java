package com.berrie.gamerental.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentGameRequest {

    @NotNull(message = "game id cannot be null")
    private String gameId;
}
