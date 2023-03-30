package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.GameModel;
import com.berrie.gamerental.dto.GetGamesRequest;
import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.dto.SubmitGameResponse;
import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.enums.SortBy;
import com.berrie.gamerental.service.GameService;
import com.berrie.gamerental.service.JwtAuthService;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Set;

import static com.berrie.gamerental.model.enums.GameStatus.AVAILABLE;
import static com.berrie.gamerental.model.enums.Genre.*;
import static com.berrie.gamerental.model.enums.Platform.*;
import static com.berrie.gamerental.model.enums.SortBy.POPULARITY;
import static com.berrie.gamerental.model.enums.SortBy.TITLE;
import static com.berrie.gamerental.util.ModelMapper.toGetGamesResponse;
import static com.berrie.gamerental.util.ModelMapper.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    private static final String SUBMIT_URI = "/api/games/submit";
    private static final String SEARCH_GAME_URI = "/api/games/search";
    private static final String GET_GAMES_URI = "/api/games";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String TOKEN = "Bearer test.token";
    private static final String TRIMMED_TOKEN = "test.token";
    private static final String USERNAME = "berrie.user";

    @Mock
    private GameService gameService;
    @Mock
    private JwtAuthService jwtAuthService;
    @InjectMocks
    private GameController gameController;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();
    }

    @Test
    void submitGame_validRequest_submitsGame() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest("Minecraft", SIMULATION, PC);
        String jsonResponse = toJson(buildSubmitGameResponse(request));
        Game game = fromSubmitGameRequest(request);

        when(jwtAuthService.extractUsername(TRIMMED_TOKEN)).thenReturn(USERNAME);
        when(gameService.submitGame(request, USERNAME)).thenReturn(game);

        // when & then
        mockMvc.perform(post(SUBMIT_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonResponse));

        verify(gameService, times(1)).submitGame(request, USERNAME);
    }

    @Test
    void submitGame_nullTitle_badRequest() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest(null, ACTION, PS4);

        // when & then
        mockMvc.perform(post(SUBMIT_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void submitGame_emptyTitle_badRequest() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest(" ", RPG, XBOX_ONE);

        // when & then
        mockMvc.perform(post(SUBMIT_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void submitGame_invalidTitle_badRequest() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest(" K", BATTLE_ROYAL, NINTENDO_SWITCH);

        // when & then
        mockMvc.perform(post(SUBMIT_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void submitGame_nullGenre_badRequest() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest("Grand Theft Auto", null, XBOX_360);

        // when & then
        mockMvc.perform(post(SUBMIT_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void submitGame_nullPlatform_badRequest() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest("Tomb Raider", SHOOTER, null);

        // when & then
        mockMvc.perform(post(SUBMIT_URI)
                        .header(AUTH_HEADER_NAME, TOKEN)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void getGames_sortedByPopularity_returnsGames() throws Exception {
        // given
        GetGamesRequest request = new GetGamesRequest(POPULARITY);
        List<GameModel> gameModels = buildGameModels(POPULARITY);
        String jsonResponse = toJson(toGetGamesResponse(gameModels));

        when(gameService.getGames(request)).thenReturn(gameModels);

        // when & then
        mockMvc.perform(get(GET_GAMES_URI)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(gameService, times(1)).getGames(request);
    }

    @Test
    void getGames_sortedByTitle_returnsGames() throws Exception {
        // given
        GetGamesRequest request = new GetGamesRequest(TITLE);
        List<GameModel> gameModels = buildGameModels(TITLE);
        String jsonResponse = toJson(toGetGamesResponse(gameModels));

        when(gameService.getGames(request)).thenReturn(gameModels);

        // when & then
        mockMvc.perform(get(GET_GAMES_URI)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(gameService, times(1)).getGames(request);
    }

    @Test
    void getGames_nullSortBy_badRequest() throws Exception {
        // given
        GetGamesRequest request = new GetGamesRequest(null);

        // when & then
        mockMvc.perform(get(GET_GAMES_URI)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(gameService);
    }

    @Test
    void getGames_noGames_notFound() throws Exception {
        // given
        GetGamesRequest request = new GetGamesRequest(POPULARITY);
        when(gameService.getGames(request)).thenThrow(new NoGamesFoundException("out of stock"));

        // when & then
        mockMvc.perform(get(GET_GAMES_URI)
                        .contentType(APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchGame_validTitleWithMatches_returnsGames() throws Exception {
        // given
        String title = "horizon";
        List<GameModel> gameMatches = List.of(buildGameModel("Horizon Zero Dawn", 2));
        String jsonResponse = toJson(toGetGamesResponse(gameMatches));

        when(gameService.searchGame(title)).thenReturn(gameMatches);

        // when & then
        mockMvc.perform(get(SEARCH_GAME_URI)
                        .param("title", title))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(gameService, times(1)).searchGame(title);
    }

    @Test
    void searchGame_validTitleNoMatches_notFound() throws Exception {
        // given
        String title = "hogwarts legacy";
        when(gameService.searchGame(title)).thenThrow(new NoGamesFoundException("no matches"));

        // when & then
        mockMvc.perform(get(SEARCH_GAME_URI)
                        .param("title", title))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchGame_invalidTitle_badRequest() throws Exception {
        // given
        String title = "  gta v";
        // in live mode, error is thrown before touching gameService
        when(gameService.searchGame(title)).thenThrow(new ConstraintViolationException("title violation", Set.of()));

        // when & then
        mockMvc.perform(get(SEARCH_GAME_URI)
                        .param("title", title))
                .andExpect(status().isBadRequest());
    }

    private SubmitGameResponse buildSubmitGameResponse(SubmitGameRequest request) throws Exception {
        return SubmitGameResponse.builder()
                .title(request.getTitle())
                .genre(request.getGenre())
                .platform(request.getPlatform())
                .build();
    }

    private Game fromSubmitGameRequest(SubmitGameRequest request) {
        return Game.builder()
                .title(request.getTitle())
                .genre(request.getGenre())
                .platform(request.getPlatform())
                .build();
    }

    private List<GameModel> buildGameModels(SortBy sortBy) {
        GameModel game1 = buildGameModel("Uncharted", 15);
        GameModel game2 = buildGameModel("Horizon Zero Dawn", 8);
        GameModel game3 = buildGameModel("Returnal", 3);

        return sortBy == SortBy.TITLE ?
                List.of(game2, game3, game1) : List.of(game1, game2, game3);
    }

    private GameModel buildGameModel(String title, Integer numberOfRentals) {
        return GameModel.builder()
                .title(title)
                .genre(ADVENTURE)
                .platform(PS5)
                .status(AVAILABLE)
                .numberOfRentals(numberOfRentals)
                .submittedBy(USERNAME)
                .build();
    }
}
