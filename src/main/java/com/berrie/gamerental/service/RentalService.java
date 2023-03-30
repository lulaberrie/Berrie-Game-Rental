package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.RentGameRequest;
import com.berrie.gamerental.exception.GameSubmissionException;
import com.berrie.gamerental.exception.GameRentedException;
import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RentalService {

    @Autowired
    private final RentalRepository rentalRepository;
    @Autowired
    private final GameService gameService;
    @Autowired
    private final AuthenticationService authService;

    /**
     * Rents a game to the user with the provided username.
     * @param request {@link RentGameRequest} object containing the id of the game to be rented.
     * @param username the username of the user renting the game.
     * @return the {@link Rental} object representing the new rental record.
     * @throws NoGamesFoundException if the game id could not be found.
     * @throws GameSubmissionException if the user tries to rent a game they submitted.
     * @throws GameRentedException if the game is already rented and unavailable for rent.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Rental rentGame(RentGameRequest request, String username) {
        final String gameId = request.getGameId();
        log.info("User {} renting game {}", username, gameId);

        User user = authService.findUserByUsername(username).get();
        Optional<Game> optionalGame = gameService.findGameById(gameId);
        if (optionalGame.isEmpty()) {
            throw new NoGamesFoundException(String.format("Game with id %s was not found", gameId));
        }
        Game game = optionalGame.get();

        if (username.equals(game.getSubmittedBy().getUsername())) {
            log.error("User {} tried to rent a game they submitted", username);
            throw new GameSubmissionException("You cannot rent a game that you submitted.");
        }
        if (game.getStatus() == GameStatus.UNAVAILABLE) {
            log.error("User {} tried renting a game {} that was already rented", username, game.getTitle());
            throw new GameRentedException("This game is not currently available to rent.");
        }

        game = gameService.rentGameCopy(game);
        Rental rental = Rental.builder()
                .rentalStatus(RentalStatus.ACTIVE)
                .rentalDate(new Date())
                .build();
        rental.setUser(user);
        rental.setGame(game);

        rentalRepository.save(rental);
        log.info("User {} successfully rented {}", username, game.getTitle());
        return rental;
    }
}
