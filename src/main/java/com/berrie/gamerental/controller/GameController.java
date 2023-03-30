package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.*;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.service.GameService;
import com.berrie.gamerental.service.JwtAuthService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.berrie.gamerental.util.ModelMapper.*;

@Validated
@RestController
@RequestMapping("/api/games")
public class GameController {

    private static final String TITLE_ERROR_MESSAGE = "Title must contain letters, numbers, " +
            "and no leading white space";

    @Autowired
    private GameService gameService;
    @Autowired
    private JwtAuthService jwtAuthService;

    /**
     * Retrieves a list of games based on the provided request parameters.
     * @param request the request containing the parameters for the game retrieval
     * @return a ResponseEntity containing the list of games
     */
    @GetMapping("")
    public ResponseEntity<GetGamesResponse> getGames(@Valid @RequestBody GetGamesRequest request) {
        List<GameModel> games = gameService.getGames(request);
        return new ResponseEntity<>(toGetGamesResponse(games), HttpStatus.OK);
    }

    /**
     * Searches for games based on the provided title parameter.
     * @param title the title to search for, consisting of letters, numbers, and no leading white space
     * @return a ResponseEntity containing the list of games matching the title
     */
    @GetMapping("/search")
    public ResponseEntity<GetGamesResponse> searchGame(@Valid @RequestParam(name = "title")
                                                           @Pattern(regexp = "^(?!\\s)[A-Za-z0-9 ]+$",
                                                                   message = TITLE_ERROR_MESSAGE) String title) {
        List<GameModel> games = gameService.searchGame(title);
        return new ResponseEntity<>(toGetGamesResponse(games), HttpStatus.OK);
    }

    /**
     * Submits a new game based on the provided request parameters and authorization token.
     * @param request the request containing the parameters for the game submission
     * @param token the authorization token used to retrieve the user's username
     * @return a ResponseEntity containing the newly created game
     */
    @PostMapping("/submit")
    public ResponseEntity<SubmitGameResponse> submitGame(@Valid @RequestBody SubmitGameRequest request,
                                                         @RequestHeader(name = "Authorization") String token) {
        Game game = gameService.submitGame(request, jwtAuthService.extractUsername(trimToken(token)));
        return new ResponseEntity<>(toSubmitGameResponse(game), HttpStatus.CREATED);
    }
}
