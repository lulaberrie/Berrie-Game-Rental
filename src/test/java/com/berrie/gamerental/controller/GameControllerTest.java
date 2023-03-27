package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.dto.SubmitGameResponse;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.service.GameService;
import com.berrie.gamerental.service.JwtAuthService;
import com.berrie.gamerental.util.ModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.berrie.gamerental.model.Genre.*;
import static com.berrie.gamerental.model.Platform.*;
import static com.berrie.gamerental.util.ModelMapper.toJson;
import static org.mockito.Mockito.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class GameControllerTest {

    private static final String SUBMIT_URI = "/api/games/submit";
    private static final String AUTH_HEADER_NAME = "Authorization";
    private static final String TOKEN = "Bearer test.token";
    private static final String TRIMMED_TOKEN = "test.token";
    private static final String USERNAME = "berrieUser";

    @Mock
    private GameService gameService;
    @Mock
    private JwtAuthService jwtAuthService;
    @InjectMocks
    private GameController gameController;
    private MockMvc mockMvc;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(gameController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();
    }

    @Test
    public void submitGame_validRequest_submitsGame() throws Exception {
        // given
        SubmitGameRequest request = new SubmitGameRequest("Minecraft", SIMULATION, PC);
        String jsonResponse = toJsonSubmitGameResponse(request);
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
    public void submitGame_nullTitle_badRequest() throws Exception {
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
    public void submitGame_emptyTitle_badRequest() throws Exception {
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
    public void submitGame_invalidTitle_badRequest() throws Exception {
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
    public void submitGame_nullGenre_badRequest() throws Exception {
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
    public void submitGame_nullPlatform_badRequest() throws Exception {
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

    private String toJsonSubmitGameResponse(SubmitGameRequest request) throws Exception {
        return ModelMapper.toJson(
                SubmitGameResponse.builder()
                        .title(request.getTitle())
                        .genre(request.getGenre())
                        .platform(request.getPlatform())
                        .build());
    }

    private Game fromSubmitGameRequest(SubmitGameRequest request) {
        return Game.builder()
                .title(request.getTitle())
                .genre(request.getGenre())
                .platform(request.getPlatform())
                .build();
    }
}
