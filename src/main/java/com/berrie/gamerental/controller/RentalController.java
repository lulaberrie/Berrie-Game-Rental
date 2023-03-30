package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.*;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.service.JwtAuthService;
import com.berrie.gamerental.service.RentalService;
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
@RequestMapping("/api/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;
    @Autowired
    private JwtAuthService jwtAuthService;

    /**
     * Rents a game to a user based on the provided game id parameter.
     * @param request the request containing the parameters for the rental
     * @param token the authorization token used to retrieve the user's username
     * @return a ResponseEntity containing the rental information
     */
    @PostMapping("/rent")
    public ResponseEntity<RentGameResponse> rentGame(@Valid @RequestBody RentGameRequest request,
                                                     @RequestHeader(name = "Authorization") String token) {
        Rental rental = rentalService.rentGame(request, jwtAuthService.extractUsername(trimToken(token)));
        return new ResponseEntity<>(toRentGameResponse(rental), HttpStatus.CREATED);
    }

    /**
     * Retrieves list of rentals for a user based on the provided game status parameter.
     * @param request the request containing the parameters for the rental retrieval
     * @param token the authorization token used to retrieve the user's username
     * @return a ResponseEntity containing the list of rentals
     */
    @GetMapping("")
    public ResponseEntity<GetRentalsResponse> getRentals(@Valid @RequestBody GetRentalsRequest request,
                                                         @RequestHeader(name = "Authorization") String token) {
        List<RentalModel> rentals = rentalService.getRentals(request, jwtAuthService.extractUsername(trimToken(token)));
        return new ResponseEntity<>(toGetRentalsResponse(rentals), HttpStatus.OK);
    }

    /**
     * Returns a game from a user based on the provided rental id parameter.
     * @param request the request containing the parameters for the return
     * @return a ResponseEntity containing the return information
     */
    @PutMapping("/return")
    public ResponseEntity<Void> returnGame(@Valid @RequestBody ReturnGameRequest request) {
        rentalService.returnGame(request);
        return ResponseEntity.noContent().build();
    }
}
