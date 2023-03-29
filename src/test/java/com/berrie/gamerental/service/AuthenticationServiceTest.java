package com.berrie.gamerental.service;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.exception.UserExistsException;
import com.berrie.gamerental.exception.UserUnauthorizedException;
import com.berrie.gamerental.model.enums.Role;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthenticationServiceTest {

    private static final String USERNAME = "berrie.user";
    private static final String PASSWORD = "pass.word";
    private static final String TOKEN = "test.token";
    private static final String ENCODED_PASSWORD = "encoded.password";

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtAuthService jwtAuthService;
    @Mock
    private Authentication authentication;
    @Mock
    private AuthenticationManager authenticationManager;
    @InjectMocks
    private AuthenticationService authenticationService;
    private AuthenticationRequest request;

    @BeforeEach
    void setup() {
        request = new AuthenticationRequest();
        request.setUsername(USERNAME);
        request.setPassword(PASSWORD);
    }

    @Test
    void createUser_newUser_returnsToken() {
        // given
        User savedUser = buildUser();

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtAuthService.generateToken(savedUser)).thenReturn(TOKEN);

        // when
        String result = authenticationService.createUser(request);

        // then
        assertThat(result).isEqualTo(TOKEN);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void createUser_existingUser_throwsUserExistsException() {
        // given
        User existingUser = buildUser();

        when(passwordEncoder.encode(PASSWORD)).thenReturn(ENCODED_PASSWORD);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(existingUser));

        // then
        assertThatThrownBy(() -> authenticationService.createUser(request)).isInstanceOf(UserExistsException.class);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void authenticateUser_validRequest_returnsToken() {
        // given
        User retrievedUser = buildUser();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(userRepository.findByUsername(USERNAME)).thenReturn(Optional.of(retrievedUser));
        when(jwtAuthService.generateToken(retrievedUser)).thenReturn(TOKEN);

        // when
        String result = authenticationService.authenticateUser(request);

        // then
        assertThat(result).isEqualTo(TOKEN);
    }

    @Test
    void authenticateUser_badCredentials_throwsUserUnauthorizedException() {
        // given
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(BadCredentialsException.class);

        // then
        assertThatThrownBy(() -> authenticationService.authenticateUser(request))
                .isInstanceOf(UserUnauthorizedException.class);
        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtAuthService);
    }

    private User buildUser() {
        return User.builder()
                .username(USERNAME)
                .password(ENCODED_PASSWORD)
                .role(Role.USER)
                .submittedGames(new ArrayList<>())
                .build();
    }
}
