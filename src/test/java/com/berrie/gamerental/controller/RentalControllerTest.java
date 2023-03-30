package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.GetRentalsRequest;
import com.berrie.gamerental.dto.RentGameRequest;
import com.berrie.gamerental.dto.RentalModel;
import com.berrie.gamerental.exception.GameRentedException;
import com.berrie.gamerental.exception.GameSubmissionException;
import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.exception.NoRentalsFoundException;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.service.JwtAuthService;
import com.berrie.gamerental.service.RentalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Date;
import java.util.List;

import static com.berrie.gamerental.util.ModelMapper.toGetRentalsResponse;
import static com.berrie.gamerental.util.ModelMapper.toJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class RentalControllerTest {

    private static final String RENT_GAME_URI = "/api/rentals/rent";
    private static final String GET_RENTALS_URI = "/api/rentals";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String TOKEN = "Bearer test.token";
    private static final String TRIMMED_TOKEN = "test.token";
    private static final String GAME_ID = "12345678";
    private static final String USERNAME = "berrie.user";

    @Mock
    private RentalService rentalService;
    @Mock
    private JwtAuthService jwtAuthService;
    @InjectMocks
    private RentalController rentalController;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(rentalController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();
    }

    @Test
    void rentGame_validRequest_rentsGame() throws Exception {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.rentGame(request, USERNAME)).thenReturn(buildRental());

        // when & then
       MvcResult result = mockMvc.perform(post(RENT_GAME_URI)
                       .header(AUTH_HEADER_NAME, TOKEN)
                       .contentType(APPLICATION_JSON)
                       .content(toJson(request)))
               .andExpect(status().isCreated())
               .andReturn();

        verify(rentalService, times(1)).rentGame(request, USERNAME);

        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody.contains("gameTitle")).isTrue();
        assertThat(responseBody.contains("dateRented")).isTrue();
    }

    @Test
    void rentGame_nullGameId_badRequest() throws Exception {
        // given
        RentGameRequest request = new RentGameRequest(null);

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    void rentGame_gameNotFound_notFound() throws Exception {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.rentGame(request, USERNAME)).thenThrow(new NoGamesFoundException("Game not found"));

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void rentGame_gameSubmittedByRenter_badRequest() throws Exception {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.rentGame(request, USERNAME))
                .thenThrow(new GameSubmissionException("renter submitted this game "));

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void rentGame_gameRented_conflict() throws Exception {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.rentGame(request, USERNAME))
                .thenThrow(new GameRentedException("game unavailable for rent"));

        // when & then
        mockMvc.perform(post(RENT_GAME_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void getRentals_activeRentals_returnsRentals() throws Exception {
        // given
        GetRentalsRequest request = new GetRentalsRequest(RentalStatus.ACTIVE);
        List<RentalModel> rentalModels = buildRentalModels(RentalStatus.ACTIVE);
        String jsonResponse = toJson(toGetRentalsResponse(rentalModels));

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.getRentals(request, USERNAME)).thenReturn(rentalModels);

        // when & then
        mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(rentalService, times(1)).getRentals(request, USERNAME);
    }

    @Test
    void getRentals_pastRentals_returnsRentals() throws Exception {
        // given
        GetRentalsRequest request = new GetRentalsRequest(RentalStatus.RETURNED);
        List<RentalModel> rentalModels = buildRentalModels(RentalStatus.RETURNED);
        String jsonResponse = toJson(toGetRentalsResponse(rentalModels));

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.getRentals(request, USERNAME)).thenReturn(rentalModels);

        // when & then
        mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(rentalService, times(1)).getRentals(request, USERNAME);
    }

    @Test
    void getRentals_nullRentalStatus_badRequest() throws Exception {
        // given
        GetRentalsRequest request = new GetRentalsRequest(null);

        // when & then
        mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(rentalService);
    }

    @Test
    void getRentals_noRentalsFound_notFound() throws Exception {
        // given
        GetRentalsRequest request = new GetRentalsRequest(RentalStatus.ACTIVE);

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(rentalService.getRentals(request, USERNAME))
                .thenThrow(new NoRentalsFoundException("no active rentals were found"));

        // when & then
        mockMvc.perform(get(GET_RENTALS_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    private Rental buildRental() {
        Game game = Game.builder()
                .title("test game")
                .build();
        Rental rental = Rental.builder()
                .rentalStatus(RentalStatus.ACTIVE)
                .rentalDate(new Date())
                .build();
        rental.setGame(game);
        return rental;
    }

    private List<RentalModel> buildRentalModels(RentalStatus rentalStatus) {
        RentalModel rental1 = buildRentalModel(rentalStatus, "Mar 2 23");
        RentalModel rental2 = buildRentalModel(rentalStatus, "Mar 10 23");
        return List.of(rental1, rental2);
    }

    private RentalModel buildRentalModel(RentalStatus rentalStatus, String dateRented) {
        return RentalModel.builder()
                .rentalStatus(rentalStatus)
                .dateRented(dateRented)
                .build();
    }
}
