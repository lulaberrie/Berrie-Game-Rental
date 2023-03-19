package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.dto.AuthenticationResponse;
import com.berrie.gamerental.service.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

//todo: validate request object
@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationService authenticationService;

    @PostMapping("/create")
    public ResponseEntity<AuthenticationResponse> createUser(@RequestBody AuthenticationRequest request) {
        return new ResponseEntity<>(authenticationService.createUser(request), HttpStatus.CREATED);
    }

    @PostMapping("/authenticate")
    public ResponseEntity<AuthenticationResponse> authenticateUser(@RequestBody AuthenticationRequest request) {
        return new ResponseEntity<>(authenticationService.authenticateUser(request), HttpStatus.OK);
    }
}
