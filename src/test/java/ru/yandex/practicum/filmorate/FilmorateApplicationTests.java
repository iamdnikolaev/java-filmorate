package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.yandex.practicum.filmorate.controller.LocalDateTypeAdapter;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Disabled
@SpringBootTest
class FilmorateApplicationTests {

    @Autowired
    private WebApplicationContext webApplicationContext;

    private MockMvc mockMvc;

    private Gson gson;

    private LocalDateTypeAdapter localDateTypeAdapter = new LocalDateTypeAdapter();

    class FilmListTypeToken extends TypeToken<List<Film>> {
    }

    class UserListTypeToken extends TypeToken<List<User>> {
    }

    class LongListTypeToken extends TypeToken<List<Long>> {
    }

    class FriendSetTypeToken extends TypeToken<Set<Long>> {
    }

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
        gson = new GsonBuilder()
                .serializeNulls()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDate.class, localDateTypeAdapter)
                .create();
    }

    @Test
    void filmCreate() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("New film")
                .description("New film description")
                .releaseDate(LocalDate.of(2024, 6, 21))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void filmsCreateAndGetAll() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("New film")
                .description("New film description")
                .releaseDate(LocalDate.of(2024, 1, 11))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        Film film2 = Film.builder()
                .id(0L)
                .name("New film 2")
                .description("New film 2 description")
                .releaseDate(LocalDate.of(2024, 2, 22))
                .duration(240)
                .build();

        filmJson = gson.toJson(film2);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        Film film3 = Film.builder()
                .id(0L)
                .name("New film 3")
                .description("New film 3 description")
                .releaseDate(LocalDate.of(2024, 3, 31))
                .duration(333)
                .build();

        filmJson = gson.toJson(film3);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        MvcResult mvcResult = mockMvc.perform(get("/films")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Film> parsedList = gson.fromJson(body, new FilmListTypeToken().getType());

        assertTrue(parsedList.size() > 0, "Фильмы не добавлены.");
        assertTrue(parsedList.contains(film), "\"New film\" не добавлен.");
        assertTrue(parsedList.contains(film2), "\"New film 2\" не добавлен.");
        assertTrue(parsedList.contains(film3), "\"New film 3\" не добавлен.");
    }

    @Test
    void filmCreateTwiceErr() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("New film")
                .description("New film description")
                .releaseDate(LocalDate.of(2023, 3, 3))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        Film film2 = Film.builder()
                .id(0L)
                .name("New film")
                .description("New film description, that already exists")
                .releaseDate(LocalDate.of(2023, 3, 3))
                .duration(130)
                .build();

        filmJson = gson.toJson(film2);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void filmUpdate() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("New film")
                .description("New film description")
                .releaseDate(LocalDate.of(2022, 2, 22))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Film filmCreated = gson.fromJson(body, Film.class);

        filmCreated.setDescription("Changed new film description");
        filmJson = gson.toJson(filmCreated);

        mockMvc.perform(put("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value("Changed new film description"));
    }

    @Test
    void filmUpdateWrongIdErr() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("New film")
                .description("New film description")
                .releaseDate(LocalDate.of(2021, 6, 23))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Film filmCreated = gson.fromJson(body, Film.class);

        filmCreated.setId(9999L);
        filmCreated.setDescription("Changed new film description");
        filmJson = gson.toJson(filmCreated);

        mockMvc.perform(put("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void filmWithoutNameErr() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .description("New film description")
                .releaseDate(LocalDate.of(2019, 6, 23))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void filmWrongDateErr() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("Old film")
                .description("Old film description")
                .releaseDate(LocalDate.of(1895, 12, 27))
                .duration(120)
                .build();

        String filmJson = gson.toJson(film);

        mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyFilmCreateErr() throws Exception {
        mockMvc.perform(post("/films")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void filmCreateAndAddLikes() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("Newest film")
                .description("Newest film description")
                .releaseDate(LocalDate.of(2024, 7, 20))
                .duration(240)
                .build();

        String filmJson = gson.toJson(film);

        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        film = gson.fromJson(body, Film.class);

        User user = User.builder()
                .id(0L)
                .email("newestuser@email.com")
                .login("newestuser")
                .name("Newest user")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user = gson.fromJson(body, User.class);

        User user2 = User.builder()
                .id(0L)
                .email("newestuser2@email.com")
                .login("newestuser2")
                .name("Newest user2")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user2 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/films/" + film.getId() + "/like/" + user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(put("/films/" + film.getId() + "/like/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Long> parsedList = gson.fromJson(body, new LongListTypeToken().getType());

        assertEquals(2, parsedList.size(), "Количество лайков неверное.");
    }

    @Test
    void filmCreateAddLikesAndDeleteOne() throws Exception {
        Film film = Film.builder()
                .id(0L)
                .name("Newest film2")
                .description("Newest film 2 description")
                .releaseDate(LocalDate.of(2024, 7, 21))
                .duration(240)
                .build();

        String filmJson = gson.toJson(film);

        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        film = gson.fromJson(body, Film.class);

        User user = User.builder()
                .id(0L)
                .email("newestuser3@email.com")
                .login("newestuser3")
                .name("Newest user3")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user = gson.fromJson(body, User.class);

        User user2 = User.builder()
                .id(0L)
                .email("newestuser4@email.com")
                .login("newestuser4")
                .name("Newest user4")
                .birthday(LocalDate.of(2002, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user2 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/films/" + film.getId() + "/like/" + user.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(put("/films/" + film.getId() + "/like/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Long> parsedList = gson.fromJson(body, new LongListTypeToken().getType());

        assertEquals(2, parsedList.size(), "Количество лайков после добавления неверное.");

        mvcResult = mockMvc.perform(delete("/films/" + film.getId() + "/like/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        parsedList = gson.fromJson(body, new LongListTypeToken().getType());
        assertEquals(1, parsedList.size(), "Количество лайков после удаления одного неверное.");
        assertEquals(user.getId(), parsedList.getFirst(), "После удаления остался лайк не того пользователя.");
    }

    @Test
    void filmCreateAddLikesAndGetPopular() throws Exception {
        Film film3 = Film.builder()
                .id(0L)
                .name("Newest film3")
                .description("Newest film 3 description")
                .releaseDate(LocalDate.of(2024, 7, 21))
                .duration(333)
                .build();

        String filmJson = gson.toJson(film3);

        MvcResult mvcResult = mockMvc.perform(post("/films")
                        .content(filmJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        film3 = gson.fromJson(body, Film.class);

        User user5 = User.builder()
                .id(0L)
                .email("newestuser5@email.com")
                .login("newestuser5")
                .name("Newest user5")
                .birthday(LocalDate.of(2005, 5, 5))
                .build();

        String userJson = gson.toJson(user5);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user5 = gson.fromJson(body, User.class);

        User user6 = User.builder()
                .id(0L)
                .email("newestuser6@email.com")
                .login("newestuser6")
                .name("Newest user6")
                .birthday(LocalDate.of(2006, 6, 6))
                .build();

        userJson = gson.toJson(user6);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user6 = gson.fromJson(body, User.class);

        User user7 = User.builder()
                .id(0L)
                .email("newestuser7@email.com")
                .login("newestuser7")
                .name("Newest user7")
                .birthday(LocalDate.of(2007, 7, 7))
                .build();

        userJson = gson.toJson(user7);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user7 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/films/" + film3.getId() + "/like/" + user5.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(put("/films/" + film3.getId() + "/like/" + user6.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(put("/films/" + film3.getId() + "/like/" + user7.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Long> parsedList = gson.fromJson(body, new LongListTypeToken().getType());

        assertEquals(3, parsedList.size(), "Количество лайков после добавления неверное.");

        mvcResult = mockMvc.perform(get("/films/popular")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Film> parsedListOfFilms = gson.fromJson(body, new FilmListTypeToken().getType());
        assertEquals(film3.getId(), parsedListOfFilms.getFirst().getId(), "Наиболее популярным стал не тот фильм.");
    }

    @Test
    void userCreate() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuser@email.com")
                .login("newuser")
                .name("New user")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());
    }

    @Test
    void usersCreateAndGetAll() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuser@email.com")
                .login("newuser1")
                .name("New user 1")
                .birthday(LocalDate.of(2011, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        User user2 = User.builder()
                .id(0L)
                .email("newuser2@email.com")
                .login("newuser2")
                .name("New user 2")
                .birthday(LocalDate.of(2012, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        User user3 = User.builder()
                .id(0L)
                .email("newuser3@email.com")
                .login("newuser3")
                .name("")
                .birthday(LocalDate.of(2013, 3, 3))
                .build();

        userJson = gson.toJson(user3);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        MvcResult mvcResult = mockMvc.perform(get("/users")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<User> parsedList = gson.fromJson(body, new UserListTypeToken().getType());

        assertTrue(parsedList.size() > 0, "Пользователи не добавлены.");
        assertTrue(parsedList.contains(user), "\"New user 1\" не добавлен.");
        assertTrue(parsedList.contains(user2), "\"New user 2\" не добавлен.");
        assertTrue(parsedList.contains(user3), "\"newuser3\" не добавлен.");
    }

    @Test
    void userCreateTwiceErr() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuserio@email.com")
                .login("newuserio")
                .name("New userio")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber());

        User user2 = User.builder()
                .id(0L)
                .email("newuserio@email.com")
                .login("newuserio")
                .name("New userio")
                .birthday(LocalDate.of(2001, 1, 1))
                .build();

        userJson = gson.toJson(user2);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void userUpdate() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("userForUpdate@email.com")
                .login("userForUpdate")
                .name("New user for update")
                .birthday(LocalDate.of(2015, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        User userCreated = gson.fromJson(body, User.class);

        userCreated.setName("NEW USER FOR UPDATE");
        userJson = gson.toJson(userCreated);

        mockMvc.perform(put("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NEW USER FOR UPDATE"));
    }

    @Test
    void userUpdateWrongIdErr() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuserWrong@email.com")
                .login("newuserWrong")
                .name("New user wrong")
                .birthday(LocalDate.of(2016, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        User userCreated = gson.fromJson(body, User.class);

        userCreated.setId(9999L);
        userCreated.setName("Changed new user wrong name");
        userJson = gson.toJson(userCreated);

        mockMvc.perform(put("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void userWithoutLoginErr() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuser@email.com")
                .name("New user")
                .birthday(LocalDate.of(2017, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void userWrongDateErr() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuseryoung@email.com")
                .login("newuseryoung")
                .name("New user young")
                .birthday(LocalDate.of(2025, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void emptyUserCreateErr() throws Exception {
        mockMvc.perform(post("/users")
                        .content("{}")
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    void userFindById() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("userToFind@email.com")
                .login("userToFind")
                .name("New user to find")
                .birthday(LocalDate.of(2015, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        User userCreated = gson.fromJson(body, User.class);

        mvcResult = mockMvc.perform(get("/users/" + userCreated.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        User userFound = gson.fromJson(body, User.class);

        assertEquals(userCreated.getEmail(), userFound.getEmail(), "Email пользователя найден неверно.");
        assertEquals(userCreated.getLogin(), userFound.getLogin(), "Логин пользователя найден неверно.");
        assertEquals(userCreated.getName(), userFound.getName(), "Имя пользователя найдено неверно.");
        assertEquals(userCreated.getBirthday(), userFound.getBirthday(),
                "Дата рождения пользователя найдена неверно.");
    }

    @Test
    void userFindByIdErr() throws Exception {
        mockMvc.perform(get("/users/9999")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());
    }

    @Test
    void userAddFriends() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuser11@email.com")
                .login("newuser11")
                .name("New user 11")
                .birthday(LocalDate.of(2011, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user = gson.fromJson(body, User.class);

        User user2 = User.builder()
                .id(0L)
                .email("newuser22@email.com")
                .login("newuser22")
                .name("New user 22")
                .birthday(LocalDate.of(2012, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user2 = gson.fromJson(body, User.class);

        User user3 = User.builder()
                .id(0L)
                .email("newuser33@email.com")
                .login("newuser33")
                .name("")
                .birthday(LocalDate.of(2013, 3, 3))
                .build();

        userJson = gson.toJson(user3);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user3 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user3.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Set<Long> parsedFriendSet = gson.fromJson(body, new FriendSetTypeToken().getType());

        assertEquals(2, parsedFriendSet.size(), "Количество друзей неверное.");
        assertTrue(parsedFriendSet.contains(user2.getId()), "\"New user 22\" не добавлен в друзья.");
        assertTrue(parsedFriendSet.contains(user3.getId()), "\"newuser33\" не добавлен в друзья.");
    }

    @Test
    void userAddAndDeleteFriends() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("newuser111@email.com")
                .login("newuser111")
                .name("New user 111")
                .birthday(LocalDate.of(2011, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user = gson.fromJson(body, User.class);

        User user2 = User.builder()
                .id(0L)
                .email("newuser222@email.com")
                .login("newuser222")
                .name("New user 222")
                .birthday(LocalDate.of(2012, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user2 = gson.fromJson(body, User.class);

        User user3 = User.builder()
                .id(0L)
                .email("newuser333@email.com")
                .login("newuser333")
                .name("")
                .birthday(LocalDate.of(2013, 3, 3))
                .build();

        userJson = gson.toJson(user3);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user3 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user3.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        Set<Long> parsedFriendSet = gson.fromJson(body, new FriendSetTypeToken().getType());

        assertEquals(2, parsedFriendSet.size(), "Количество друзей после добавления неверное.");
        assertTrue(parsedFriendSet.contains(user2.getId()), "\"New user 222\" не добавлен в друзья.");
        assertTrue(parsedFriendSet.contains(user3.getId()), "\"newuser333\" не добавлен в друзья.");

        mockMvc.perform(delete("/users/" + user.getId() + "/friends/" + user3.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(delete("/users/" + user.getId() + "/friends/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        parsedFriendSet = gson.fromJson(body, new FriendSetTypeToken().getType());

        assertEquals(0, parsedFriendSet.size(), "Количество друзей после удаления неверное.");
    }

    @Test
    void userAddAndGetAllFriends() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("new1user@email.com")
                .login("new1user")
                .name("New 1 user")
                .birthday(LocalDate.of(2011, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user = gson.fromJson(body, User.class);

        User user2 = User.builder()
                .id(0L)
                .email("new2user@email.com")
                .login("new2user")
                .name("New 2 user")
                .birthday(LocalDate.of(2012, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user2 = gson.fromJson(body, User.class);

        User user3 = User.builder()
                .id(0L)
                .email("new3user@email.com")
                .login("new3user")
                .name("")
                .birthday(LocalDate.of(2013, 3, 3))
                .build();

        userJson = gson.toJson(user3);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user3 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user3.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<Long> parsedFriendIdList = gson.fromJson(body, new LongListTypeToken().getType());

        assertEquals(2, parsedFriendIdList.size(), "Количество друзей после добавления неверное.");
        assertTrue(parsedFriendIdList.contains(user2.getId()), "\"New user 22\" не добавлен в друзья.");
        assertTrue(parsedFriendIdList.contains(user3.getId()), "\"newuser33\" не добавлен в друзья.");

        mvcResult = mockMvc.perform(get("/users/" + user.getId() + "/friends")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<User> parsedList = gson.fromJson(body, new UserListTypeToken().getType());

        assertTrue(parsedList.stream().map(User::getId).toList().containsAll(parsedFriendIdList),
                "Список друзей после добавления не совпадает с отдельно полученным.");
    }

    @Test
    void userAddAndGetCommonFriends() throws Exception {
        User user = User.builder()
                .id(0L)
                .email("new11user@email.com")
                .login("new11user")
                .name("New 11 user")
                .birthday(LocalDate.of(2011, 1, 1))
                .build();

        String userJson = gson.toJson(user);

        MvcResult mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        String body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user = gson.fromJson(body, User.class);

        User user2 = User.builder()
                .id(0L)
                .email("new22user@email.com")
                .login("new22user")
                .name("New 22 user")
                .birthday(LocalDate.of(2012, 2, 2))
                .build();

        userJson = gson.toJson(user2);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user2 = gson.fromJson(body, User.class);

        User user3 = User.builder()
                .id(0L)
                .email("new33user@email.com")
                .login("new33user")
                .name("")
                .birthday(LocalDate.of(2013, 3, 3))
                .build();

        userJson = gson.toJson(user3);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user3 = gson.fromJson(body, User.class);

        User user4 = User.builder()
                .id(0L)
                .email("new44user@email.com")
                .login("new44user")
                .name("")
                .birthday(LocalDate.of(2014, 4, 4))
                .build();

        userJson = gson.toJson(user4);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user4 = gson.fromJson(body, User.class);

        User user5 = User.builder()
                .id(0L)
                .email("new55user@email.com")
                .login("new55user")
                .name("New 55 user")
                .birthday(LocalDate.of(2015, 5, 5))
                .build();

        userJson = gson.toJson(user5);

        mvcResult = mockMvc.perform(post("/users")
                        .content(userJson)
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        user5 = gson.fromJson(body, User.class);

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user3.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/" + user.getId() + "/friends/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/" + user5.getId() + "/friends/" + user4.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/" + user5.getId() + "/friends/" + user3.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mockMvc.perform(put("/users/" + user5.getId() + "/friends/" + user2.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        mvcResult = mockMvc.perform(get("/users/" + user.getId() + "/friends/common/" + user5.getId())
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        body = mvcResult.getResponse().getContentAsString(StandardCharsets.UTF_8);
        List<User> parsedFriendsList = gson.fromJson(body, new UserListTypeToken().getType());

        assertEquals(2, parsedFriendsList.size(), "Количество общих друзей после добавления неверное.");
        assertTrue(parsedFriendsList.contains(user2),
                "\"New user 22\" отсутствует в общих друзьях пользователей \"New 11 user\" и \"New 55 user\"");
        assertTrue(parsedFriendsList.contains(user3),
                "\"newuser33\" отсутствует в общих друзьях пользователей \"New 11 user\" и \"New 55 user\"");

    }

}
