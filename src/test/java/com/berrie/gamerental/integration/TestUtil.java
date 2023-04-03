package com.berrie.gamerental.integration;

import com.berrie.gamerental.dto.AuthenticationRequest;
import com.berrie.gamerental.model.Game;
import com.berrie.gamerental.model.Rental;
import com.berrie.gamerental.model.User;
import com.berrie.gamerental.model.enums.GameStatus;
import com.berrie.gamerental.model.enums.Genre;
import com.berrie.gamerental.model.enums.Platform;
import com.berrie.gamerental.model.enums.RentalStatus;
import com.berrie.gamerental.repository.GameRepository;
import com.berrie.gamerental.repository.RentalRepository;
import com.berrie.gamerental.repository.UserRepository;
import com.berrie.gamerental.util.ModelMapper;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Date;
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

    public static void deleteRental(String rentedBy, RentalRepository rentalRepository) {
        Optional<Rental> rental = rentalRepository.findByRentedBy(rentedBy);
        assertThat(rental).isPresent();
        rentalRepository.delete(rental.get());
    }

    public static void deleteRentals(String rentedBy, RentalRepository rentalRepository) {
        rentalRepository.deleteAllByRentedBy(rentedBy);
    }

    public static void deleteUser(User user, UserRepository userRepository) {
        userRepository.delete(user);
    }

    public static void saveGame(Game game, GameRepository gameRepository) {
        gameRepository.save(game);
    }

    public static void saveGames(List<Game> games, GameRepository gameRepository) {
        gameRepository.saveAll(games);
    }

    public static void saveRental(Rental rental, RentalRepository rentalRepository) {
        rentalRepository.save(rental);
    }

    public static void saveRentals(List<Rental> rentals, RentalRepository rentalRepository) {
        rentalRepository.saveAll(rentals);
    }

    public static User findUser(String username, UserRepository userRepository) {
        Optional<User> user = userRepository.findByUsername(username);
        assertThat(user).isPresent();
        return user.get();
    }

    public static List<User> findUsers(String username, UserRepository userRepository) {
        return userRepository.findAllByUsername(username);
    }

    public static Game findGame(String title, GameRepository gameRepository) {
        Optional<Game> game = gameRepository.findByTitle(title);
        assertThat(game).isPresent();
        return game.get();
    }

    public static Rental findRental(String rentedBy, RentalRepository rentalRepository) {
        Optional<Rental> rental = rentalRepository.findByRentedBy(rentedBy);
        assertThat(rental).isPresent();
        return rental.get();
    }

    public static String getJson(MvcResult mvcResult) throws Exception {
        return mvcResult.getResponse().getContentAsString();
    }

    public static Game buildGame(String title) {
        return Game.builder()
                .title(title)
                .genre(Genre.RPG)
                .platform(Platform.XBOX_360)
                .status(GameStatus.AVAILABLE)
                .numberOfRentals(15)
                .submittedBy(User.builder()
                        .username("berrie.user")
                        .build())
                .build();
    }

    public static Rental buildRental(String username, RentalStatus rentalStatus, Game game, Date returnDate, User user) {
        return Rental.builder()
                .rentalStatus(rentalStatus)
                .user(user)
                .game(game)
                .rentalDate(new Date())
                .returnDate(returnDate)
                .rentedBy(username)
                .build();
    }
}
