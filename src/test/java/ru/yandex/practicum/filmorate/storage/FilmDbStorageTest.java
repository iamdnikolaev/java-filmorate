package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.FilmExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmGenresExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmLikesExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmMpaExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import({FilmDbStorage.class, FilmRowMapper.class, FilmGenresExtractor.class, FilmMpaExtractor.class,
        FilmLikesExtractor.class, FilmExtractor.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("FilmDbStorage")
class FilmDbStorageTest {
    private static final long TEST_FILM_ID = 1L;
    private static final long TEST_NEWFILM_ID = 4L;
    private final FilmDbStorage filmDbStorage;

    static Film getTestFilm() {
        Rating rating = new Rating();
        rating.setId((long) 1);
        rating.setName("G");

        Film film = Film.builder()
                .id(TEST_FILM_ID)
                .name("Фильм 1")
                .description("Самый интересный фильм 1")
                .releaseDate(LocalDate.of(1999, 1, 2))
                .duration(111)
                .mpa(rating)
                .build();

        return film;
    }

    static List<Film> getTestFilmList() {
        List<Film> resultList = new ArrayList<>();
        String[] ratingArray = {"G", "PG", "PG-13"};

        for (int i = 1; i < 4; i++) {
            Rating rating = new Rating();
            rating.setId((long) i);
            rating.setName(ratingArray[i - 1]);

            Film film = Film.builder()
                    .id((long) i)
                    .name("Фильм " + i)
                    .description("Самый интересный фильм " + i)
                    .releaseDate(LocalDate.of(1999, 1, 1).plusDays(i))
                    .duration(111 * i)
                    .mpa(rating)
                    .build();

            resultList.add(film);
        }
        return resultList;
    }

    @Test
    @DisplayName("Должен найти все фильмы")
    void should_return_all_filims() {
        List<Film> testFilmList = getTestFilmList();

        Optional<Collection<Film>> filmsOptional = Optional.ofNullable(filmDbStorage.findAll());
        assertTrue(filmsOptional.isPresent(), "Фильмы получены.");
        assertEquals(testFilmList.size(), filmsOptional.get().size(), "Неверное количество фильмов.");
        List<Film> foundFilmList = filmsOptional.get().stream().toList();

        for (int i = 0; i < testFilmList.size(); i++) {
            assertThat(foundFilmList.get(i))
                    .usingRecursiveComparison()
                    .ignoringExpectedNullFields()
                    .isEqualTo(testFilmList.get(i));
        }
    }


    @Test
    @DisplayName("Должен найти фильм по id = 1")
    void should_return_film_when_find_by_id() {
        Film film = filmDbStorage.findById(TEST_FILM_ID);

        assertThat(film)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(getTestFilm());
    }


    @Test
    @DisplayName("Должен создать фильм 4")
    void should_create_new_film() {
        Rating rating = new Rating();
        rating.setId((long) 1);
        rating.setName("G");

        Film newFilm = Film.builder()
                .id(TEST_NEWFILM_ID)
                .name("Фильм 4")
                .description("Самый интересный фильм 4")
                .releaseDate(LocalDate.of(1999, 1, 5))
                .duration(444)
                .mpa(rating)
                .build();
        newFilm = filmDbStorage.create(newFilm);

        Film madeFilm = filmDbStorage.findById(newFilm.getId());

        assertThat(madeFilm)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(newFilm);
    }

    @Test
    @DisplayName("Должен изменить атрибуты имеющегося фильма 1")
    void should_update_film() {
        Film oldFilm = filmDbStorage.findById(1);
        oldFilm.setName(oldFilm.getName() + "changed");
        oldFilm.setDescription(oldFilm.getDescription() + "changed");
        oldFilm.setReleaseDate(oldFilm.getReleaseDate().plusDays(1));
        oldFilm.setDuration(oldFilm.getDuration() + 1);

        Film changedFilm = filmDbStorage.update(oldFilm);
        changedFilm = filmDbStorage.findById(oldFilm.getId());

        assertThat(changedFilm)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(oldFilm);
    }

    @Test
    @DisplayName("Должен добавить лайк пользователя 2 фильму 3")
    void should_add_like_to_film() {
        List<Long> likes = filmDbStorage.addLike(3, 2);
        HashSet<Long> likesExpected = new HashSet<>(likes);
        Film film = filmDbStorage.findById(3);

        assertEquals(likesExpected, film.getLikesUserId(), "Лайк выставлен.");
    }

    @Test
    @DisplayName("Должен убрать лайк пользователя 1 у фильма 3")
    void should_delete_like_of_film() {
        Film film = filmDbStorage.findById(3);

        List<Long> likes = filmDbStorage.deleteLike(3, 1);
        assertEquals(film.getLikesUserId().size() - 1, likes.size(), "Неверное количество лайков после удаления. Должно быть 0.");
    }

    @Test
    @DisplayName("Должен вывести список популярных фильмов (1, 2, 3)")
    void should_get_popular_films() {
        List<Film> films = filmDbStorage.getPopularFilms(10);
        assertEquals(2, films.get(0).getId(), "Самый популярный фильм 2");
        assertEquals(1, films.get(1).getId(), "Средний по популярности фильм 1");
        assertEquals(3, films.get(2).getId(), "Самый непопулярный фильм 3");
    }
}