package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.UserRepository;
import com.berrie.gamerental.util.ModelMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

class TestUtil {

    private static final String CREATE_URI = "/api/auth/create";

    public static String createUser(String username, MockMvc mockMvc) throws Exception {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .username(username)
                .password("12345678")
                .build();

        MvcResult result = mockMvc.perform(post(CREATE_URI)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andReturn();

        String jsonResponse = getJson(result);
        return JsonPath.read(jsonResponse, "$.token");
    }

    public static void deleteUser(String username, UserRepository userRepository) {
        Optional<User> user = userRepository.findByUsername(username);
        assertThat(user).isPresent();
        userRepository.delete(user.get());
    }

    public static void deleteGame(String title, GameRepository gameRepository) {
        Optional<Game> game = gameRepository.findByTitle(title);
        assertThat(game).isPresent();
        gameRepository.delete(game.get());
    }

    public static void deleteGames(List<Game> games, GameRepository gameRepository) {
        games.stream()
                .map(Game::getTitle)
                .forEach(title -> deleteGame(title, gameRepository));
    }

    public static String getJson(MvcResult mvcResult) throws Exception {
        return mvcResult.getResponse().getContentAsString();
    }
}
