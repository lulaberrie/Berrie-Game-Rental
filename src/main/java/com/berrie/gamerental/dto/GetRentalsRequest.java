package com.berrie.gamerental.dto;

import com.berrie.gamerental.model.enums.RentalStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetRentalsRequest {

    @NotNull(message = "rentalStatus cannot be null")
    private RentalStatus rentalStatus;
}
