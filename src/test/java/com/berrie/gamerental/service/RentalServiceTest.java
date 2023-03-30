package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.GetRentalsRequest;
import com.berrie.gamerental.dto.RentGameRequest;
import com.berrie.gamerental.dto.RentalModel;
import com.berrie.gamerental.dto.ReturnGameRequest;
import com.berrie.gamerental.exception.*;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.repository.RentalRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.berrie.gamerental.model.enums.RentalStatus.ACTIVE;
import static com.berrie.gamerental.model.enums.RentalStatus.RETURNED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class RentalServiceTest {

    private static final String USERNAME = "berrie.user";
    private static final String OTHER_USER = "user.ale";
    private static final String GAME_ID = "12345678";;
    private static final String RENTAL_ID = "87654321";


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
        verify(rentalRepository).insert(captor.capture());
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

    @Test
    void getRentals_activeRentals_returnsRentalModels() {
        // given
        GetRentalsRequest request = new GetRentalsRequest(ACTIVE);
        List<Rental> rentalList = buildRentalList(ACTIVE);

        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(rentalRepository.findByUserAndRentalStatusOrderByRentalDateDesc(buildUser(USERNAME), ACTIVE))
                .thenReturn(rentalList);

        // when
        List<RentalModel> result = rentalService.getRentals(request, USERNAME);

        // then
        assertThat(result).hasSize(2);
        RentalModel actual = result.get(0);
        Rental expected = rentalList.get(0);
        assertRentalModel(actual, expected);
        assertThat(actual.getDateReturned()).isNull();
    }

    @Test
    void getRentals_pastRentals_returnsRentalModels() {
        // given
        GetRentalsRequest request = new GetRentalsRequest(RETURNED);
        List<Rental> rentalList = buildRentalList(RETURNED);

        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(rentalRepository.findByUserAndRentalStatusOrderByRentalDateDesc(buildUser(USERNAME), RETURNED))
                .thenReturn(rentalList);

        // when
        List<RentalModel> result = rentalService.getRentals(request, USERNAME);

        // then
        assertThat(result).hasSize(2);
        RentalModel actual = result.get(0);
        Rental expected = rentalList.get(0);
        assertRentalModel(actual, expected);
        assertThat(actual.getDateReturned()).isNotNull();
    }

    @Test
    void getRentals_noRentalsFound_throwsNoRentalsFoundException() {
        // given
        GetRentalsRequest request = new GetRentalsRequest(ACTIVE);

        when(authService.findUserByUsername(USERNAME)).thenReturn(Optional.of(buildUser(USERNAME)));
        when(rentalRepository.findByUserAndRentalStatusOrderByRentalDateDesc(buildUser(USERNAME), ACTIVE))
                .thenReturn(new ArrayList<>());

        // when & then
        assertThatThrownBy(() -> rentalService.getRentals(request, USERNAME))
                .isInstanceOf(NoRentalsFoundException.class);
    }

    @Test
    void returnGame_validRequest_returnsGame() {
        // given
        ReturnGameRequest request = new ReturnGameRequest(RENTAL_ID);
        Rental rental = buildRental(ACTIVE);
        Game game = buildGame("Returnal", Genre.ADVENTURE, Platform.PS5);
        rental.setUser(buildUser(USERNAME));
        rental.setGame(game);

        when(rentalRepository.findRentalById(RENTAL_ID)).thenReturn(Optional.of(rental));
        doNothing().when(gameService).returnGameCopy(any(Game.class));

        // when
        rentalService.returnGame(request);

        // then
        ArgumentCaptor<Rental> captor = ArgumentCaptor.forClass(Rental.class);
        verify(rentalRepository).findRentalById(RENTAL_ID);
        verify(gameService).returnGameCopy(game);
        verify(rentalRepository).save(captor.capture());
        Rental result = captor.getValue();
        assertThat(result.getReturnDate()).isNotNull();
        assertThat(result.getRentalStatus()).isEqualTo(RETURNED);
    }

    @Test
    void returnGame_noRentalFound_throwsNoRentalsFoundException() {
        // given
        ReturnGameRequest request = new ReturnGameRequest(RENTAL_ID);
        when(rentalRepository.findRentalById(RENTAL_ID)).thenReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> rentalService.returnGame(request)).isInstanceOf(NoRentalsFoundException.class);
    }

    @Test
    void returnGame_gameAlreadyReturned_throwsGameReturnedException() {
        // given
        ReturnGameRequest request = new ReturnGameRequest(RENTAL_ID);
        Rental rental = buildRental(RETURNED);
        rental.setUser(buildUser(USERNAME));

        when(rentalRepository.findRentalById(RENTAL_ID)).thenReturn(Optional.of(rental));

        // when & then
        assertThatThrownBy(() -> rentalService.returnGame(request)).isInstanceOf(GameReturnedException.class);
    }

    private void assertRental(Rental rental) {
        assertThat(rental.getRentalStatus()).isEqualTo(ACTIVE);
        assertThat(rental.getRentalDate()).isNotNull();
        assertThat(rental.getUser()).isEqualTo(buildUser(USERNAME));
        assertThat(rental.getGame()).isEqualTo(buildGame(1, GameStatus.UNAVAILABLE, OTHER_USER));
    }

    private void assertRentalModel(RentalModel actual, Rental expected) {
        Game expectedGame = expected.getGame();
        assertThat(actual.getRentalStatus()).isEqualTo(expected.getRentalStatus());
        assertThat(actual.getGameTitle()).isEqualTo(expectedGame.getTitle());
        assertThat(actual.getGameGenre()).isEqualTo(expectedGame.getGenre());
        assertThat(actual.getGamePlatform()).isEqualTo(expectedGame.getPlatform());
        assertThat(actual.getDateRented()).isNotNull();
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

    private Game buildGame(String title, Genre genre, Platform platform) {
        return Game.builder()
                .title(title)
                .genre(genre)
                .platform(platform)
                .build();
    }

    private List<Rental> buildRentalList(RentalStatus rentalStatus) {
        Rental rental1 = buildRental(rentalStatus);
        rental1.setGame(buildGame("UFC", Genre.SPORTS, Platform.PC));
        Rental rental2 = buildRental(rentalStatus);
        rental2.setGame(buildGame("COD", Genre.SHOOTER, Platform.PS5));
        return List.of(rental1, rental2);
    }

    private Rental buildRental(RentalStatus rentalStatus) {
        Rental rental = Rental.builder()
                .rentalStatus(rentalStatus)
                .rentalDate(new Date())
                .build();
        if (rentalStatus == RETURNED) {
            rental.setReturnDate(new Date(System.currentTimeMillis() + TimeUnit.HOURS.toMillis(2)));
        }
        return rental;
    }
}
