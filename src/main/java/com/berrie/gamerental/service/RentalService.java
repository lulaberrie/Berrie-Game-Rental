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
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.repository.RentalRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import static com.berrie.gamerental.util.ModelMapper.toRentalModelList;

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
        rental.setRentedBy(username);

        rentalRepository.insert(rental);
        log.info("User {} successfully rented {}", username, game.getTitle());
        return rental;
    }

    /**
     * Retrieves a list of rentals for a given user according to the specified rental status.
     * @param request {@link GetRentalsRequest} object containing the rental status to filter by.
     * @param username the username of the user whose rentals are being retrieved.
     * @return list of {@link RentalModel} objects sorted in descending order of the rental date.
     * @throws NoRentalsFoundException if no rental records were found.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public List<RentalModel> getRentals(GetRentalsRequest request, String username) {
        final RentalStatus rentalStatus = request.getRentalStatus();
        log.info("Fetching {} rentals for user {}", rentalStatus.toString().toLowerCase(), username);

        User user = authService.findUserByUsername(username).get();
        List<Rental> rentalList = rentalRepository.findByUserAndRentalStatusOrderByRentalDateDesc(user, rentalStatus);

        if (rentalList.isEmpty()) {
            log.error("no rental records were found for user {}", username);
            String state = rentalStatus == RentalStatus.ACTIVE ? "active" : "past";
            throw new NoRentalsFoundException(String.format("Looks like you don't have any %s rentals.", state));
        }

        log.info("Returning {} {} rentals for {}", rentalList.size(), rentalStatus, username);
        return toRentalModelList(rentalList);
    }

    /**
     * Returns a game rented by the returning user.
     * @param request {@link ReturnGameRequest} object containing the rental id to be returned.
     * @throws NoRentalsFoundException if no rental was found.
     * @throws GameRentedException if the rental is in the returned status.
     */
    public void returnGame(ReturnGameRequest request) {
        final String rentalId = request.getRentalId();
        log.info("Returning rental {}", rentalId);

        Optional<Rental> optionalRental = rentalRepository.findRentalById(rentalId);
        if (optionalRental.isEmpty()) {
            throw new NoRentalsFoundException(String.format("Rental with id %s was not found", rentalId));
        }
        Rental rental = optionalRental.get();
        String username = rental.getUser().getUsername();

        if (rental.getRentalStatus() == RentalStatus.RETURNED) {
            log.error("{} tried to return a game they have already returned", username);
            throw new GameReturnedException("This game has already been returned by you");
        }
        Game gameCopy = rental.getGame();
        rental.setReturnDate(new Date());
        rental.setRentalStatus(RentalStatus.RETURNED);

        rentalRepository.save(rental);
        gameService.returnGameCopy(gameCopy);
        log.info("{} successfully returned by {}", gameCopy.getId(), username);
    }
}
