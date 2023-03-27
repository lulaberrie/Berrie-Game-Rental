package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.GameStatus;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameService {

    @Autowired
    private final GameRepository gameRepository;
    @Autowired
    private final UserRepository userRepository;

    /**
     * Method to submit a new game with the provided details and username.
     * @param request request object containing details of the game to be submitted.
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
}
