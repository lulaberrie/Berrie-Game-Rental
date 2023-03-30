package com.berrie.gamerental.util;

import com.berrie.gamerental.dto.*;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

    public static RentGameResponse toRentGameResponse(Rental rental) {
        return RentGameResponse.builder()
                .gameTitle(rental.getGame().getTitle())
                .dateRented(dateToPrettyString(rental.getRentalDate()))
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

    private static String dateToPrettyString(Date date) {
        DateTimeFormatter inputFormatter = DateTimeFormatter
                .ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("MMMM dd uuuu", Locale.ENGLISH);
        ZonedDateTime zonedDateTime = ZonedDateTime.parse(date.toString(), inputFormatter);
        return outputFormatter.format(zonedDateTime);
    }
}
