package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.dto.SubmitGameResponse;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.repository.GameRepository;
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

import static com.berrie.gamerental.integration.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubmitGameIntegrationTest {

    private static final String SUBMIT_URI = "/api/games/submit";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void submitGame_validRequest_gameIsSubmitted() throws Exception {
        // given
        String username = "submit.one", title = "SubmitTestOne";
        SubmitGameRequest request = new SubmitGameRequest(title, Genre.ADVENTURE, Platform.PS5);
        String token = "Bearer " + createUser(username, mockMvc);

        // when
        MvcResult result = mockMvc.perform(post(SUBMIT_URI)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated())
                .andReturn();

        // then
        SubmitGameResponse response = ModelMapper.fromJson(getJson(result), SubmitGameResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getGenre()).isEqualTo(request.getGenre());
        assertThat(response.getPlatform()).isEqualTo(request.getPlatform());

        Game savedGame = findGame(title, gameRepository);
        assertGame(savedGame, request.getTitle(), request.getGenre(), request.getPlatform(), username);

        // clean up
        deleteUser(username, userRepository);
        deleteGame(title, gameRepository);
    }

    private void assertGame(Game game, String expectedTitle, Genre expectedGenre, Platform expectedPlatform,
                            String expectedSubmittedBy) {
        assertThat(game.getTitle()).isEqualTo(expectedTitle);
        assertThat(game.getGenre()).isEqualTo(expectedGenre);
        assertThat(game.getPlatform()).isEqualTo(expectedPlatform);
        assertThat(game.getNumberOfRentals()).isEqualTo(0);
        assertThat(game.getSubmittedBy().getUsername()).isEqualTo(expectedSubmittedBy);
    }
}
