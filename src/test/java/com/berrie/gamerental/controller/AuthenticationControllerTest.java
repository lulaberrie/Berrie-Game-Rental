package com.berrie.gamerental.controller;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.dto.AuthenticationResponse;
import com.berrie.gamerental.exception.UserExistsException;
import com.berrie.gamerental.exception.UserUnauthorizedException;
import com.berrie.gamerental.service.AuthenticationService;
import com.berrie.gamerental.util.ModelMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthenticationControllerTest {

    private static final String CREATE_URI = "/api/auth/create";
    private static final String AUTHENTICATE_URI = "/api/auth/authenticate";
    private static final String VALID_USERNAME = "berrieUser";
    private static final String INVALID_USERNAME = "17-38";
    private static final String VALID_PASSWORD = "pass1234";
    private static final String INVALID_PASSWORD = "short";
    private static final String JSON_WEB_TOKEN = "mockToken";
    private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;

    @Mock
    private AuthenticationService authenticationService;
    @InjectMocks
    private AuthenticationController authenticationController;
    private AuthenticationRequest request;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(authenticationController)
                .setControllerAdvice(new ExceptionHandlerController())
                .build();

        request = AuthenticationRequest.builder()
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .build();
    }

    @Test
    void createUser_validRequest_createsUserWithToken() throws Exception {
        // given
        String jsonResponse = ModelMapper.toJson(
                AuthenticationResponse.builder()
                        .token(JSON_WEB_TOKEN)
                        .build());

        when(authenticationService.createUser(request)).thenReturn(JSON_WEB_TOKEN);

        // when & then
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(content().json(jsonResponse));

        verify(authenticationService, times(1)).createUser(request);
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void createUser_nullUsername_badRequest() throws Exception {
        // given
        request.setUsername(null);

        // when & then
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void createUser_invalidUsername_badRequest() throws Exception {
        // given
        request.setUsername(INVALID_USERNAME);

        // when & then
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void createUser_userExists_conflict() throws Exception {
        // given
        when(authenticationService.createUser(request)).thenThrow(new UserExistsException("User already exists"));

        // when & then
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isConflict());
    }

    @Test
    void authenticateUser_validRequest_authenticatesUserWithToken() throws Exception {
        // given
        String jsonResponse = ModelMapper.toJson(
                AuthenticationResponse.builder()
                        .token(JSON_WEB_TOKEN)
                        .build());

        when(authenticationService.authenticateUser(request)).thenReturn(JSON_WEB_TOKEN);

        // when & then
        mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().json(jsonResponse));

        verify(authenticationService, times(1)).authenticateUser(request);
        verifyNoMoreInteractions(authenticationService);
    }

    @Test
    void authenticateUser_nullPassword_badRequest() throws Exception {
        // given
        request.setPassword(null);

        // when & then
        mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void authenticateUser_invalidPassword_badRequest() throws Exception {
        // given
        request.setPassword(INVALID_PASSWORD);

        // when & then
        mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(authenticationService);
    }

    @Test
    void authenticateUser_IncorrectUsernameOrPassword_unauthorized() throws Exception {
        // given
        when(authenticationService.authenticateUser(request)).thenThrow(new UserUnauthorizedException("User unauthorized"));

        // when & then
        mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isUnauthorized());
    }
}
