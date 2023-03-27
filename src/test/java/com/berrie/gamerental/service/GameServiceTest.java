package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.GameModel;
import com.berrie.gamerental.dto.GetGamesRequest;
import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.model.enums.SortBy;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.berrie.gamerental.model.enums.GameStatus.AVAILABLE;
import static com.berrie.gamerental.model.enums.GameStatus.UNAVAILABLE;
import static com.berrie.gamerental.model.enums.Genre.ADVENTURE;
import static com.berrie.gamerental.model.enums.Genre.SPORTS;
import static com.berrie.gamerental.model.enums.Platform.PS5;
import static com.berrie.gamerental.model.enums.Platform.XBOX_ONE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {
    private static final String USERNAME = "berrieUser";
    private static final String TITLE = "FIFA 23";

    @Mock
    private GameRepository gameRepository;
    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private GameService gameService;

    @Test
    void submitGame_validRequest_returnsSavedGame() {
        // given
        SubmitGameRequest request = SubmitGameRequest.builder()
                .title(TITLE)
                .genre(Genre.SPORTS)
                .platform(Platform.PS5)
                .build();

        when(userRepository.findByUsername(USERNAME))
                .thenReturn(Optional.of(buildUser()));

        // when
        Game result = gameService.submitGame(request, USERNAME);

        // then
        ArgumentCaptor<Game> captor = ArgumentCaptor.forClass(Game.class);
        verify(gameRepository).save(captor.capture());
        Game savedGame = captor.getValue();
        assertThat(result).isEqualTo(savedGame);
        assertGame(result);
    }

    @Test
    void getGames_sortByPopularity_returnsGameModelsInOrder() {
        // given
        GetGamesRequest request = new GetGamesRequest(SortBy.POPULARITY);
        List<Game> gameList = buildGameList(SortBy.POPULARITY);

        when(gameRepository.findAllByOrderByNumberOfRentalsDesc()).thenReturn(gameList);

        // when
        List<GameModel> result = gameService.getGames(request);

        // then
        Game expectedGame = gameList.get(1);
        GameModel gameModel = result.get(1);
        assertGameModel(gameModel, expectedGame);
        verify(gameRepository, never()).findAllByOrderByTitleAsc();
    }

    @Test
    void getGames_sortByTitle_returnsGameModelsInOrder() {
        // given
        GetGamesRequest request = new GetGamesRequest(SortBy.TITLE);
        List<Game> gameList = buildGameList(SortBy.TITLE);

        when(gameRepository.findAllByOrderByTitleAsc()).thenReturn(gameList);

        // when
        List<GameModel> result = gameService.getGames(request);

        // then
        Game expectedGame = gameList.get(2);
        GameModel gameModel = result.get(2);
        assertGameModel(gameModel, expectedGame);
        verify(gameRepository, never()).findAllByOrderByNumberOfRentalsDesc();
    }

    @Test
    void getGames_noGames_throwsNoGamesFoundException() {
        // given
        GetGamesRequest request = new GetGamesRequest(SortBy.POPULARITY);
        when(gameRepository.findAllByOrderByNumberOfRentalsDesc()).thenReturn(new ArrayList<>());

        // when & then
        assertThatThrownBy(() -> gameService.getGames(request))
                .isInstanceOf(NoGamesFoundException.class);
    }

    private void assertGame(Game game) {
        assertThat(game.getTitle()).isEqualTo(GameServiceTest.TITLE);
        assertThat(game.getGenre()).isEqualTo(Genre.SPORTS);
        assertThat(game.getPlatform()).isEqualTo(Platform.PS5);
        assertThat(game.getStatus()).isEqualTo(GameStatus.AVAILABLE);
        assertThat(game.getNumberOfRentals()).isEqualTo(0);
        assertThat(game.getSubmittedBy()).isEqualTo(buildUser());
    }

    private void assertGameModel(GameModel actual, Game expected) {
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getGenre()).isEqualTo(expected.getGenre());
        assertThat(actual.getPlatform()).isEqualTo(expected.getPlatform());
        assertThat(actual.getStatus()).isEqualTo(expected.getStatus());
        assertThat(actual.getNumberOfRentals()).isEqualTo(expected.getNumberOfRentals());
        assertThat(actual.getSubmittedBy()).isEqualTo(USERNAME);
    }

    private User buildUser() {
        return User.builder()
                .username(USERNAME)
                .build();
    }

    private List<Game> buildGameList(SortBy sortBy) {
        Game game1 = buildGame("Uncharted", 15, ADVENTURE, PS5, AVAILABLE);
        Game game2 = buildGame("Horizon", 8, SPORTS, XBOX_ONE, UNAVAILABLE);
        Game game3 = buildGame("Skyrim", 3, Genre.RPG, Platform.PC, AVAILABLE );

        return sortBy == SortBy.TITLE ?
                List.of(game2, game3, game1) : List.of(game1, game2, game3);
    }

    private Game buildGame(String title, Integer numberOfRentals, Genre genre,
                           Platform platform, GameStatus status) {
        return Game.builder()
                .title(title)
                .genre(genre)
                .platform(platform)
                .status(status)
                .numberOfRentals(numberOfRentals)
                .submittedBy(buildUser())
                .build();
    }
}
