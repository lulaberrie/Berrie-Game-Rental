package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.GameModel;
import com.berrie.gamerental.dto.GetGamesResponse;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.util.ModelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;

import static com.berrie.gamerental.integration.TestUtil.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class SearchGameIntegrationTest {

    private static final String SEARCH_GAME_URI = "/api/games/search";

    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private MockMvc mockMvc;

    @Test
    void searchGame_titleWithMatches_returnsGamesInDescOrder() throws Exception {
        // given
        Game game1 = buildGame("Search Game One"), game2 = buildGame("Search Game Two");
        saveGames(List.of(game1, game2), gameRepository);

        // when
        MvcResult result = mockMvc.perform(get(SEARCH_GAME_URI)
                        .param("title", "search game"))
                .andExpect(status().isOk())
                .andReturn();

        // then
        List<GameModel> searchResults = ModelMapper.fromJson(getJson(result), GetGamesResponse.class).getGames();
        assertThat(searchResults.size()).isGreaterThanOrEqualTo(2);

        // clean up
        deleteGames(List.of(game1, game2), gameRepository);
    }

    @Test
    void searchGame_titleWithNoMatches_doesNotReturnGames() throws Exception {
        // given & when & then
        mockMvc.perform(get(SEARCH_GAME_URI)
                        .param("title", "noTitlesFound"))
                .andExpect(status().isNotFound());
    }

    @Test
    void searchGame_invalidTitle_doesNotSearchGame() throws Exception {
        // given & when & then
        mockMvc.perform(get(SEARCH_GAME_URI)
                .param("title", "  spaces"))
                .andExpect(status().isBadRequest());
    }
}
