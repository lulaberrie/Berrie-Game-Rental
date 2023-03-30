package com.berrie.gamerental.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RentGameResponse {
    private String gameTitle;
    private String dateRented;
}
