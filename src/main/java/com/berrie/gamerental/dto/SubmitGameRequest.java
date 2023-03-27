package com.berrie.gamerental.dto;

import com.berrie.gamerental.model.Genre;
import com.berrie.gamerental.model.Platform;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubmitGameRequest {

    @NotBlank(message = "Title cannot be blank")
    @Pattern(regexp = "^[a-zA-Z0-9][a-zA-Z0-9 ]*[a-zA-Z0-9]$",
            message = "Game title must only contain letters, numbers, no spaces before, " +
                    "and single spaces between")
    @Size(min = 2, max = 50, message = "Game title must be between 2 and 50 characters long")
    private String title;

    @NotNull(message = "Pick a genre")
    private Genre genre;

    @NotNull(message = "Pick a platform")
    private Platform platform;
}
