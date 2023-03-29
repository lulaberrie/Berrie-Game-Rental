package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.model.enums.Role;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.repository.UserRepository;
import com.berrie.gamerental.util.ModelMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(SpringExtension.class)
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
public class AuthenticationControllerIntegrationTest {

    private static final String CREATE_URI = "/api/auth/create";
    private static final String AUTHENTICATE_URI = "/api/auth/authenticate";
    private static final String VALID_USERNAME = "berrie.user";
    private static final String VALID_PASSWORD = "pass1234";
    private static final MediaType APPLICATION_JSON = MediaType.APPLICATION_JSON;

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    void createUser_validRequest_createsUser() throws Exception {
        // given
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(VALID_USERNAME)
                .password(VALID_PASSWORD)
                .build();

        // when
        mockMvc.perform(post(CREATE_URI)
                .contentType(APPLICATION_JSON)
                .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(APPLICATION_JSON));

        // then
        Optional<User> savedUser = userRepository.findByUsername(VALID_USERNAME);
        assertThat(savedUser).isPresent();
        assertUserCredentials(savedUser.get());
        userRepository.delete(savedUser.get());
    }

    @Test
    void createUser_userExists_doesNotCreateUser() throws Exception {
        // given
        String berrieUser = "user.two";
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(berrieUser)
                .password(VALID_PASSWORD)
                .build();

        // create user
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated());

        // when
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isConflict());

        // then
        List<User> users = userRepository.findAllByUsername(berrieUser);
        assertThat(users).hasSize(1);
        userRepository.delete(users.get(0));
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void authenticateUser_validRequest_authenticatesUser() throws Exception {
        // given
        String berrieUser = "user.three";
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(berrieUser)
                .password(VALID_PASSWORD)
                .build();

        // create user
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated());

        // when
        mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(APPLICATION_JSON));

        // then
        Optional<User> authenticatedUser = userRepository.findByUsername(berrieUser);
        userRepository.delete(authenticatedUser.get());

    }

    @Test
    void authenticateUser_userDoesNotExist_doesNotAuthenticateUser() throws Exception {
        // given
        String berrieUser = "user.four";
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(berrieUser)
                .password(VALID_PASSWORD)
                .build();

        // when
        MvcResult result = mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // then
        assertNoTokenReturned(result);
    }

    @SuppressWarnings("OptionalGetWithoutIsPresent")
    @Test
    void authenticateUser_existingUserIncorrectPassword_doesNotAuthenticateUser() throws Exception {
        // given
        String berrieUser = "user.five";
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(berrieUser)
                .password(VALID_PASSWORD)
                .build();

        // create user
        mockMvc.perform(post(CREATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isCreated());

        request.setPassword("wrongPassword");

        // when
        MvcResult result = mockMvc.perform(post(AUTHENTICATE_URI)
                        .contentType(APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andExpect(status().isUnauthorized())
                .andReturn();

        // then
        assertNoTokenReturned(result);
        Optional<User> user = userRepository.findByUsername(berrieUser);
        userRepository.delete(user.get());
    }

    private void assertUserCredentials(User user) {
        assertThat(user.getUsername()).isEqualTo(VALID_USERNAME);
        assertThat(user.getPassword()).isNotEqualTo(VALID_PASSWORD);
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getSubmittedGames()).isNotNull();
        assertThat(user.isAccountNonExpired()).isTrue();
        assertThat(user.isAccountNonLocked()).isTrue();
        assertThat(user.isCredentialsNonExpired()).isTrue();
        assertThat(user.isEnabled()).isTrue();
    }

    private void assertNoTokenReturned(MvcResult result) throws Exception {
        String responseBody = result.getResponse().getContentAsString();
        assertThat(responseBody.contains("jsonWebToken")).isFalse();
    }
}
