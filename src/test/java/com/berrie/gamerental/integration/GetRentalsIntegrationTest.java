package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.GetRentalsRequest;
import com.berrie.gamerental.dto.GetRentalsResponse;
import com.berrie.gamerental.dto.RentalModel;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
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
import java.util.List;

import static com.berrie.gamerental.integration.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetRentalsIntegrationTest {

    private static final String GET_RENTALS_URI = "/api/rentals";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String USERNAME = "berrie.user";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private RentalRepository rentalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private GameRepository gameRepository;

    @Test
    void getRentals_withActiveRentals_returnsActiveRentals() throws Exception {
        // given
        GetRentalsRequest request = new GetRentalsRequest(RentalStatus.ACTIVE);
        String token = "Bearer " + createUser(USERNAME, mockMvc);
        setupRentals();

        // when
        MvcResult result = mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<RentalModel> rentalModels = ModelMapper.fromJson(getJson(result), GetRentalsResponse.class).getRentals();
        assertThat(rentalModels.size()).isEqualTo(2);
        assertThat(rentalModels).noneMatch(rental -> rental.getRentalStatus() == RentalStatus.RETURNED);

        // clean up
        deleteUser(USERNAME, userRepository);
        deleteRentals(USERNAME, rentalRepository);
        deleteGames(setupGames(), gameRepository);
    }

    @Test
    void getRentals_withPastRentals_returnsPastRentals() throws Exception {
        // given
        GetRentalsRequest request = new GetRentalsRequest(RentalStatus.RETURNED);
        String token = "Bearer " + createUser(USERNAME, mockMvc);
        setupRentals();

        // when
        MvcResult result = mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<RentalModel> rentalModels = ModelMapper.fromJson(getJson(result), GetRentalsResponse.class).getRentals();
        assertThat(rentalModels.size()).isEqualTo(1);
        assertThat(rentalModels).noneMatch(rental -> rental.getRentalStatus() == RentalStatus.ACTIVE);

        // clean up
        deleteUser(USERNAME, userRepository);
        deleteRentals(USERNAME, rentalRepository);
        deleteGames(setupGames(), gameRepository);
    }

    @Test
    void getRentals_noRentalsFound_returnsNotFound() throws Exception {
        // given
        String userWithNoRentals = "rental.user";
        GetRentalsRequest request = new GetRentalsRequest(RentalStatus.ACTIVE);
        String token = "Bearer " + createUser(userWithNoRentals, mockMvc);

        // when
        mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isNotFound());

        // clean up
        deleteUser(userWithNoRentals, userRepository);
    }

    private void setupRentals() {
        List<Game> games = setupGames();
        saveGames(games, gameRepository);
        User user = findUser(USERNAME, userRepository);
        Rental rental1 = buildRental(USERNAME, RentalStatus.ACTIVE, games.get(0), null, user);
        Rental rental2 = buildRental(USERNAME, RentalStatus.RETURNED, games.get(1), new Date(), user);
        Rental rental3 = buildRental(USERNAME, RentalStatus.ACTIVE, games.get(2), null, user);
        List<Rental> rentals = List.of(rental1, rental2, rental3);
        saveRentals(rentals, rentalRepository);
    }

    private List<Game> setupGames() {
        Game game1 = buildGame("Spiderman");
        Game game2 = buildGame("Final Fantasy");
        Game game3 = buildGame("COD");
        return List.of(game1, game2, game3);
    }
}
