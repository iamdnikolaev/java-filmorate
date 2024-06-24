package ru.yandex.practicum.filmorate;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.junit.jupiter.api.BeforeEach;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
}
