package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.dto.SubmitGameResponse;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.service.GameService;
import com.berrie.gamerental.service.JwtAuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.berrie.gamerental.util.ModelMapper.toSubmitGameResponse;
import static com.berrie.gamerental.util.ModelMapper.trimToken;

@Validated
@RestController
@RequestMapping("/api/games")
public class GameController {

    @Autowired
    private GameService gameService;
    @Autowired
    private JwtAuthService jwtAuthService;

    @PostMapping("/submit")
    public ResponseEntity<SubmitGameResponse> submitGame(@Valid @RequestBody SubmitGameRequest request,
                                                         @RequestHeader(name = "Authorization") String token) {
        Game game = gameService.submitGame(request, jwtAuthService.extractUsername(trimToken(token)));
        return new ResponseEntity<>(toSubmitGameResponse(game), HttpStatus.CREATED);
    }
}
