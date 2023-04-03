package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.ReturnGameRequest;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
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

import java.util.Date;

import static com.berrie.gamerental.integration.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReturnGameIntegrationTest {

    private static final String RETURN_GAME_URI = "/api/rentals/return";
    private static final String USERNAME = "rent.user";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;

    @Test
    void returnGame_validRequest_returnsGame() throws Exception {
        // given
        Rental rental = setupRental(RentalStatus.ACTIVE, null, GameStatus.UNAVAILABLE);
        ReturnGameRequest request = new ReturnGameRequest(rental.getId());

        // when
        mockMvc.perform(put(RETURN_GAME_URI)
                .contentType(MediaType.APPLICATION_JSON)
                .content(ModelMapper.toJson(request)))
                .andExpect(status().isNoContent());

        // then
        Rental savedRental = findRental(USERNAME, rentalRepository);
        assertThat(savedRental.getReturnDate()).isNotNull();
        assertThat(savedRental.getRentalStatus()).isEqualTo(RentalStatus.RETURNED);
        assertThat(savedRental.getGame().getStatus()).isEqualTo(GameStatus.AVAILABLE);

        // clean up
        deleteUser(rental.getUser(), userRepository);
        deleteGame(rental.getGame().getTitle(), gameRepository);
        deleteRental(USERNAME, rentalRepository);
    }

    @Test
    void returnGame_gameAlreadyReturned_returnsIsConflict() throws Exception {
        // given
        Rental rental = setupRental(RentalStatus.RETURNED, new Date(), GameStatus.AVAILABLE);
        ReturnGameRequest request = new ReturnGameRequest(rental.getId());

        // when & then
        mockMvc.perform(put(RETURN_GAME_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isConflict());

        // clean up
        deleteUser(rental.getUser(), userRepository);
        deleteGame(rental.getGame().getTitle(), gameRepository);
        deleteRental(USERNAME, rentalRepository);
    }

    @Test
    void returnGame_noRentalFound_returnsNotFound() throws Exception {
        // given
        ReturnGameRequest request = new ReturnGameRequest("rental54321");

        // when & then
        mockMvc.perform(put(RETURN_GAME_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isNotFound());
    }


    private Rental setupRental(RentalStatus rentalStatus, Date returnDate, GameStatus gameStatus) throws Exception {
        createUser(USERNAME, mockMvc);
        User user = findUser(USERNAME, userRepository);
        Game game = buildGame("The Quarry");
        game.setStatus(gameStatus);
        saveGame(game, gameRepository);
        Rental rental = buildRental(USERNAME, rentalStatus, game, returnDate, user);
        rental.setId("rental1234567");
        saveRental(rental, rentalRepository);
        return rental;
    }
}
