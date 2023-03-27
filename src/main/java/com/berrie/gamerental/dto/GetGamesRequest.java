package com.berrie.gamerental.dto;

import com.berrie.gamerental.model.enums.SortBy;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetGamesRequest {

    @NotNull(message = "sortBy cannot be null")
    private SortBy sortBy;
}
