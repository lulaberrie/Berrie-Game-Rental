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
public class ReturnGameRequest {

    @NotNull(message = "rentalId cannot be null")
    private String rentalId;
}
