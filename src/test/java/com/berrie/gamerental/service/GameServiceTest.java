package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.model.*;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    public void submitGame_validRequest_returnsSavedGame() {
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

    private void assertGame(Game game) {
        assertThat(game.getTitle()).isEqualTo(GameServiceTest.TITLE);
        assertThat(game.getGenre()).isEqualTo(Genre.SPORTS);
        assertThat(game.getPlatform()).isEqualTo(Platform.PS5);
        assertThat(game.getStatus()).isEqualTo(GameStatus.AVAILABLE);
        assertThat(game.getNumberOfRentals()).isEqualTo(0);
        assertThat(game.getSubmittedBy()).isEqualTo(buildUser());
    }

    private User buildUser() {
        return User.builder()
                .username(USERNAME)
                .build();
    }
}
