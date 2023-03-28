package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.*;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.service.GameService;
import com.berrie.gamerental.service.JwtAuthService;
import jakarta.validation.Valid;
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

    @Autowired
    private GameService gameService;
    @Autowired
    private JwtAuthService jwtAuthService;

    @GetMapping("")
    public ResponseEntity<GetGamesResponse> getGames(@Valid @RequestBody GetGamesRequest request) {
        List<GameModel> games = gameService.getGames(request);
        return new ResponseEntity<>(toGetGamesResponse(games), HttpStatus.OK);
    }

    @PostMapping("/submit")
    public ResponseEntity<SubmitGameResponse> submitGame(@Valid @RequestBody SubmitGameRequest request,
                                                         @RequestHeader(name = "Authorization") String token) {
        Game game = gameService.submitGame(request, jwtAuthService.extractUsername(trimToken(token)));
        return new ResponseEntity<>(toSubmitGameResponse(game), HttpStatus.CREATED);
    }
}
