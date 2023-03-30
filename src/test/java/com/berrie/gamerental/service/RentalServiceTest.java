package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.RentGameRequest;
import com.berrie.gamerental.exception.GameRentedException;
import com.berrie.gamerental.exception.GameSubmissionException;
import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    private static final String USERNAME = "berrie.user";
    private static final String OTHER_USER = "user.ale";
    private static final String GAME_ID = "12345678";


    @Mock
    private RentalRepository rentalRepository;
    @Mock
    private GameService gameService;
    @Mock
    private AuthenticationService authService;
    @InjectMocks
    private RentalService rentalService;

    @Test
    void rentGame_validRequest_returnsRental() {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);
        Game game = buildGame(0, GameStatus.AVAILABLE, OTHER_USER);
        Game rentedGame = buildGame(1, GameStatus.UNAVAILABLE, OTHER_USER);

        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(gameService.findGameById(GAME_ID)).thenReturn(Optional.of(game));
        when(gameService.rentGameCopy(game)).thenReturn(rentedGame);

        // when
        Rental result = rentalService.rentGame(request, USERNAME);

        // then
        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).save(captor.capture());
        Rental savedRental = captor.getValue();
        assertThat(result).isEqualTo(savedRental);
        assertRental(result);
    }

    @Test
    void rentGame_gameNotFound_throwsNoGamesFoundException() {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);
        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(gameService.findGameById(GAME_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rentalService.rentGame(request, USERNAME)).isInstanceOf(NoGamesFoundException.class);
    }

    @Test
    void rentGame_gameSubmittedByRenter_throwsGameSubmissionException() {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);
        Game gameSubmittedByRenter = buildGame(0, GameStatus.AVAILABLE, USERNAME);

        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(gameService.findGameById(GAME_ID)).thenReturn(Optional.of(gameSubmittedByRenter));

        // when & then
        assertThatThrownBy(() -> rentalService.rentGame(request, USERNAME)).isInstanceOf(GameSubmissionException.class);
    }

    @Test
    void rentGame_gameRented_throwsGameRentedException() {
        // given
        RentGameRequest request = new RentGameRequest(GAME_ID);
        Game unavailableGame = buildGame(0, GameStatus.UNAVAILABLE, OTHER_USER);

        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(gameService.findGameById(GAME_ID)).thenReturn(Optional.of(unavailableGame));

        // when & then
        assertThatThrownBy(() -> rentalService.rentGame(request, USERNAME)).isInstanceOf(GameRentedException.class);
    }

    private void assertRental(Rental rental) {
        assertThat(rental.getRentalStatus()).isEqualTo(RentalStatus.ACTIVE);
        assertThat(rental.getRentalDate()).isNotNull();
        assertThat(rental.getUser()).isEqualTo(buildUser(USERNAME));
        assertThat(rental.getGame()).isEqualTo(buildGame(1, GameStatus.UNAVAILABLE, OTHER_USER));
    }

    private User buildUser(String username) {
        return User.builder()
                .username(username)
                .build();
    }

    private Game buildGame(Integer numberOfRentals, GameStatus status, String submittedBy) {
        return Game.builder()
                .id(GAME_ID)
                .title("Uncharted")
                .numberOfRentals(numberOfRentals)
                .status(status)
                .submittedBy(buildUser(submittedBy))
                .build();
    }
}
