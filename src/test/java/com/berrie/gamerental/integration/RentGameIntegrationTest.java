package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.RentGameRequest;
import com.berrie.gamerental.dto.RentGameResponse;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.RentalRepository;
import com.berrie.gamerental.repository.UserRepository;
import com.berrie.gamerental.util.ModelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;

import static com.berrie.gamerental.integration.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class RentGameIntegrationTest {

    private static final String RENT_GAME_URI = "/api/rentals/rent";
    private static final String AUTH_HEADER_NAME = "Authorization";

    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void rentGame_validRequest_rentsGame() throws Exception {
        // given
        String renter = "rental.user";
        String token = "Bearer " + createUser(renter, mockMvc), gameId = "test123456";
        Game game = buildGame("rentGameOne");
        game.setId(gameId);
        saveGame(game, gameRepository);
        String prettyDate = ModelMapper.dateToPrettyString(new Date());
        RentGameRequest request = new RentGameRequest(gameId);

        // when
        MvcResult result = mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        RentGameResponse response = ModelMapper.fromJson(getJson(result), RentGameResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getGameTitle()).isEqualTo(game.getTitle());
        assertThat(response.getDateRented()).isEqualTo(prettyDate);

        Rental savedRental = findRental(renter, rentalRepository);
        assertRental(savedRental, renter, game);

        // clean up
        deleteUser(renter, userRepository);
        deleteGame(game.getTitle(), gameRepository);
        deleteRental(renter, rentalRepository);
    }

    private void assertRental(Rental rental, String expectedRentedBy, Game expectedGame) {
        assertThat(rental.getRentalStatus()).isEqualTo(RentalStatus.ACTIVE);
        assertThat(rental.getUser().getUsername()).isEqualTo(expectedRentedBy);
        assertThat(rental.getGame().getTitle()).isEqualTo(expectedGame.getTitle());
        assertThat(rental.getGame().getNumberOfRentals()).isEqualTo(expectedGame.getNumberOfRentals() + 1);
        assertThat(rental.getGame().getStatus()).isEqualTo(GameStatus.UNAVAILABLE);
        assertThat(rental.getRentalDate()).isNotNull();
        assertThat(rental.getReturnDate()).isNull();
        assertThat(rental.getRentedBy()).isEqualTo(expectedRentedBy);
    }

    @Test
    void rentGame_gameNotFound_returnsNotFound() throws Exception {
        // given
        String username = "rental.two";
        String token = "Bearer " + createUser(username, mockMvc);
        RentGameRequest request = new RentGameRequest("test87654");

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isNotFound());

        // clean up
        deleteUser(username, userRepository);
    }

    @Test
    void rentGame_gameSubmittedByUser_doesNotRentGame() throws Exception {
        // given
        String gameSubmittedBy = "berrie.user";
        String token = "Bearer " + createUser(gameSubmittedBy, mockMvc), gameId = "test123456";
        Game game = buildGame("rentGameTwo");
        game.setId(gameId);
        saveGame(game, gameRepository);
        RentGameRequest request = new RentGameRequest(gameId);

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isBadRequest());

        // clean up
        deleteUser(gameSubmittedBy, userRepository);
        deleteGame(game.getTitle(), gameRepository);
    }

    @Test
    void rentGame_gameRented_doesNotRentGame() throws Exception {
        // given
        String username = "rental.three";
        String token = "Bearer " + createUser(username, mockMvc), gameId = "test56341";
        Game game = buildGame("rentGameThree");
        game.setId(gameId);
        game.setStatus(GameStatus.UNAVAILABLE);
        saveGame(game, gameRepository);
        RentGameRequest request = new RentGameRequest(gameId);

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isConflict());

        // clean up
        deleteUser(username, userRepository);
        deleteGame(game.getTitle(), gameRepository);
    }
}
