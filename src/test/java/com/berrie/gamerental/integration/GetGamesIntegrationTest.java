package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.GameModel;
import com.berrie.gamerental.dto.GetGamesRequest;
import com.berrie.gamerental.dto.GetGamesResponse;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.model.enums.SortBy;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.UserRepository;
import com.berrie.gamerental.util.ModelMapper;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.TypeRef;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Comparator;
import java.util.List;

import static com.berrie.gamerental.integration.TestUtil.deleteGames;
import static com.berrie.gamerental.integration.TestUtil.getJson;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class GetGamesIntegrationTest {

    private static final String GET_GAMES_URI = "/api/games";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void getGames_sortedByPopularity_returnsGamesInOrder() throws Exception {
        // given
        GetGamesRequest request = new GetGamesRequest(SortBy.POPULARITY);
        List<Game> games = setupGames("A", "C", "B");

        // when
        MvcResult result = mockMvc.perform(get(GET_GAMES_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andReturn();

        // then
        List<GameModel> gameModels = ModelMapper.fromJson(getJson(result), GetGamesResponse.class).getGames();
        GameModel game1 = gameModels.stream()
                .filter(gameModel -> gameModel.getTitle().equals("GetGamesA"))
                .toList()
                .get(0);
        assertGameModel(game1);
        List<Integer> numberOfRentalsList = JsonPath.read(getJson(result), "$[*].numberOfRentals");
        assertThat(numberOfRentalsList).isSortedAccordingTo(Comparator.reverseOrder());
        deleteGames(games, gameRepository);
    }

    @Test
    void getGames_sortedByTitle_returnsGamesInOrder() throws Exception {
        // given
        GetGamesRequest request = new GetGamesRequest(SortBy.POPULARITY);
        List<Game> games = setupGames("E", "F", "D");

        // when
        MvcResult result = mockMvc.perform(get(GET_GAMES_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andReturn();

        // then
        List<Integer> numberOfRentalsList = JsonPath.read(getJson(result), "$[*].title");
        assertThat(numberOfRentalsList).isSorted();
        deleteGames(games, gameRepository);
    }

    private void assertGameModel(GameModel gameModel) {
        assertThat(gameModel.getTitle()).isEqualTo("GetGamesA");
        assertThat(gameModel.getGenre()).isEqualTo(Genre.ACTION);
        assertThat(gameModel.getPlatform()).isEqualTo(Platform.XBOX_ONE);
        assertThat(gameModel.getStatus()).isEqualTo(GameStatus.AVAILABLE);
        assertThat(gameModel.getNumberOfRentals()).isEqualTo(8);
        assertThat(gameModel.getSubmittedBy()).isEqualTo("berrie.user");
    }

    private List<Game> setupGames(String one, String two, String three) {
        Game game1 = buildGame("GetGames" + one, 8);
        Game game2 = buildGame("GetGames" + two, 12);
        Game game3 = buildGame("GetGames" + three, 4);
        gameRepository.saveAll(List.of(game1, game2, game3));
        return List.of(game1, game2, game3);
    }

    private Game buildGame(String title, Integer numberOfRentals) {
        return Game.builder()
                .title(title)
                .genre(Genre.ACTION)
                .platform(Platform.XBOX_ONE)
                .status(GameStatus.AVAILABLE)
                .numberOfRentals(numberOfRentals)
                .submittedBy(User.builder().username("berrie.user").build())
                .build();
    }
}
