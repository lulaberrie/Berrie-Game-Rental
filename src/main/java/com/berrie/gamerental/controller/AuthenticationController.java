package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.dto.AuthenticationResponse;
import com.berrie.gamerental.service.AuthenticationService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.berrie.gamerental.util.ModelMapper.*;

@Validated
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/create")
    public ResponseEntity<AuthenticationResponse> createUser(@Valid @RequestBody AuthenticationRequest request) {
        String jsonWebToken = authenticationService.createUser(request);
        return new ResponseEntity<>(toAuthenticationResponse(jsonWebToken), HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@Valid @RequestBody AuthenticationRequest request) {
        String jsonWebToken = authenticationService.authenticateUser(request);
        return new ResponseEntity<>(toAuthenticationResponse(jsonWebToken), HttpStatus.OK);
    }
}
