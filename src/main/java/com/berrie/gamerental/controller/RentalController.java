package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.RentGameRequest;
import com.berrie.gamerental.dto.RentGameResponse;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.service.JwtAuthService;
import com.berrie.gamerental.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.berrie.gamerental.util.ModelMapper.toRentGameResponse;
import static com.berrie.gamerental.util.ModelMapper.trimToken;

@Validated
@RestController
@RequestMapping("/api/rentals")
public class RentalController {

    @Autowired
    private RentalService rentalService;
    @Autowired
    private JwtAuthService jwtAuthService;

    /**
     * Rents a game to a user.
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
}
