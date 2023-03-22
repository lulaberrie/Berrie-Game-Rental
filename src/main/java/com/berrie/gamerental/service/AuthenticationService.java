package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.exception.UserExistsException;
import com.berrie.gamerental.exception.UserNotFoundException;
import com.berrie.gamerental.model.Role;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    @Autowired
    private final UserRepository userRepository;
    @Autowired
    private final PasswordEncoder passwordEncoder;
    @Autowired
    private final JwtAuthService jwtAuthService;
    @Autowired
    private final AuthenticationManager authenticationManager;

    /**
     * Method to create a new user provided the user does not already exist.
     * @param request request object containing the required fields to create a user.
     * @return JWT generated token.
     */
    public String createUser(AuthenticationRequest request) {
        final String username = request.getUsername();
        log.info("Creating new user with username {}", username);
        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.USER)
                .build();
        if (userRepository.findByUsername(username).isEmpty()) {
            userRepository.save(user);
        } else throw new UserExistsException(String.format("User with username %s already exists", username));


        log.info("User with username {} successfully created", username);
        return jwtAuthService.generateToken(user);
    }

    /**
     * Method to log in a returning user provided the user already exists.
     * @param request request object containing the required fields to authenticate a user.
     * @return JWT generated token.
     */
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    public String authenticateUser(AuthenticationRequest request) {
        final String username = request.getUsername();
        log.info("Authenticating user with username {}", username);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                    username,
                    request.getPassword()
            ));
        } catch (BadCredentialsException ex) {
            log.error("User with username {} was not authenticated", username);
            throw new UserNotFoundException(String.format("Either the User %s or password is incorrect", username));
        }

        log.info("User with username {} successfully authenticated", username);
        User user = userRepository.findByUsername(username).get();
        return jwtAuthService.generateToken(user);
    }
}
