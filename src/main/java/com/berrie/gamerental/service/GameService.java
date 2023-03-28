package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.GameModel;
import com.berrie.gamerental.dto.GetGamesRequest;
import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.exception.NoGamesFoundException;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.SortBy;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

import static com.berrie.gamerental.util.ModelMapper.toGameModelList;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    @Autowired
    private final GameRepository gameRepository;
    @Autowired
    private final UserRepository userRepository;

    /**
     * Submits a new game with the provided details and username.
     * @param request {@link SubmitGameRequest} object containing details of the game to be submitted.
     * @param username the username of the user submitting the game.
     * @return the submitted game.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public Game submitGame(SubmitGameRequest request, String username) {
        final String title = request.getTitle();
        log.info("User {} submitting game {}", username, title);

        User user = userRepository.findByUsername(username).get();
        Game game = Game.builder()
                .title(title)
                .genre(request.getGenre())
                .platform(request.getPlatform())
                .status(GameStatus.AVAILABLE)
                .numberOfRentals(0)
                .submittedBy(user)
                .build();

        gameRepository.save(game);
        log.info("Game with title {} successfully submitted", title);
        return game;
    }

    /**
     * Retrieves a list of all games and sorts them according to the specified sort order.
     * @param request {@link GetGamesRequest} object containing the sort by preference for the games.
     * @return list of {@link GameModel} objects sorted according to the sort order.
     * @throws NoGamesFoundException if no games are found.
     */
    public List<GameModel> getGames(GetGamesRequest request) {
        final SortBy sortBy = request.getSortBy();
        List<Game> gameList = new ArrayList<>();

        if (SortBy.POPULARITY == sortBy) {
            log.info("Fetching games sorted by popularity");
            gameList = gameRepository.findAllByOrderByNumberOfRentalsDesc();
        } else if (SortBy.TITLE == sortBy) {
            log.info("Fetching games sorted by title");
            gameList = gameRepository.findAllByOrderByTitleAsc();
        }

        if (gameList.isEmpty()) {
            log.error("store is out of stock, no games were found");
            throw new NoGamesFoundException("No games in stock, check back at a later time!");
        }

        log.info("Returning {} games", gameList.size());
        return toGameModelList(gameList);
    }
}
