package com.berrie.gamerental.util;

import com.berrie.gamerental.dto.AuthenticationResponse;
import com.berrie.gamerental.dto.GameModel;
import com.berrie.gamerental.dto.GetGamesResponse;
import com.berrie.gamerental.dto.SubmitGameResponse;
import com.berrie.gamerental.model.Game;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.stream.Collectors;

public class ModelMapper {

    public static AuthenticationResponse toAuthenticationResponse(String token) {
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public static SubmitGameResponse toSubmitGameResponse(Game game) {
        return SubmitGameResponse.builder()
                .title(game.getTitle())
                .genre(game.getGenre())
                .platform(game.getPlatform())
                .build();
    }

    public static GetGamesResponse toGetGamesResponse(List<GameModel> gameModelList) {
        return GetGamesResponse.builder()
                .games(gameModelList)
                .build();
    }

    public static List<GameModel> toGameModelList(List<Game> gameList) {
        return gameList.stream()
                .map(ModelMapper::toGameModel)
                .collect(Collectors.toList());
    }

    public static GameModel toGameModel(Game game) {
        return GameModel.builder()
                .title(game.getTitle())
                .genre(game.getGenre())
                .platform(game.getPlatform())
                .status(game.getStatus())
                .numberOfRentals(game.getNumberOfRentals())
                .submittedBy(game.getSubmittedBy().getUsername())
                .build();
    }

    public static String trimToken(String jsonWebToken) {
        return jsonWebToken.substring(7);
    }

    public static String toJson(Object obj) throws JsonProcessingException {
        return new ObjectMapper().writeValueAsString(obj);
    }
}
