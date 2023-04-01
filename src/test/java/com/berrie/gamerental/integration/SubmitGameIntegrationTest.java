package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.SubmitGameRequest;
import com.berrie.gamerental.dto.SubmitGameResponse;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.repository.GameRepository;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@ExtendWith(SpringExtension.class)
@AutoConfigureMockMvc(addFilters = false)
public class SubmitGameIntegrationTest {

    private static final String SUBMIT_URI = "/api/games/submit";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private GameRepository gameRepository;
    @Autowired
    private UserRepository userRepository;

    @Test
    void submitGame_validRequest_gameIsSubmitted() throws Exception {
        // given
        String username = "submit.one", title = "SubmitTestOne";
        SubmitGameRequest request = new SubmitGameRequest(title, Genre.ADVENTURE, Platform.PS5);
        String token = "Bearer " + TestUtil.createUser(username, mockMvc);

        // when
        MvcResult result = mockMvc.perform(post(SUBMIT_URI)
                        .header("Authorization", token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ModelMapper.toJson(request)))
                .andReturn();

        // then
        SubmitGameResponse response = ModelMapper.fromJson(TestUtil.getJson(result), SubmitGameResponse.class);
        assertThat(response).isNotNull();
        assertThat(response.getTitle()).isEqualTo(request.getTitle());
        assertThat(response.getGenre()).isEqualTo(request.getGenre());
        assertThat(response.getPlatform()).isEqualTo(request.getPlatform());

        // clean up
        TestUtil.deleteUser(username, userRepository);
        TestUtil.deleteGame(title, gameRepository);
    }
}
