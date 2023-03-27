package com.berrie.gamerental.dto;

import com.berrie.gamerental.model.Genre;
import com.berrie.gamerental.model.Platform;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class SubmitGameResponse {

    private String title;
    private Genre genre;
    private Platform platform;
}
