package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.mappers.FilmExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmGenresExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmLikesExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.FilmMpaExtractor;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Реализация репозитория работы с фильмами с хранением в базе данных.
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Repository
@Primary
@Slf4j
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {
    private static final String INSERT_QUERY = "INSERT INTO films (name, description, release_date, duration, mpa)" +
            "VALUES (:name, :description, :releaseDate, :duration, :mpa)";
    private static final String UPDATE_QUERY = "UPDATE films SET name = :name, description = :description, " +
            "release_date = :releaseDate, duration = :duration, mpa = :mpa " +
            "WHERE film_id = :filmId";
    private static final String INSERT_GENRES = "INSERT INTO film_genres (film_id, genre_id) VALUES (:filmId, :genreId)";
    private static final String FIND_ALL_QUERY = "SELECT * FROM films";
    private static final String FIND_BY_ID_QUERY = "SELECT f.film_id, f.name AS film_name, f.description, " +
            "f.release_date, f.duration, r.rating_id, r.name AS rating_name, g.genre_id, g.name AS genre_name " +
            "FROM films f " +
            "LEFT JOIN ratings r ON f.mpa = r.rating_id " +
            "LEFT JOIN film_genres fg ON f.film_id = fg.film_id " +
            "LEFT JOIN genres g ON fg.genre_id = g.genre_id " +
            "WHERE f.film_id = :filmId";
    private static final String FIND_ALL_FILM_GENRES = "SELECT fg.film_id, fg.genre_id, g.name AS genre_name " +
            "FROM film_genres fg " +
            "INNER JOIN genres g ON fg.genre_id = g.genre_id";
    private static final String FIND_ALL_MPA = "SELECT f.film_id, f.mpa, r.name AS mpa_name " +
            "FROM films f " +
            "INNER JOIN ratings r ON f.mpa = r.rating_id";
    private static final String FIND_ALL_LIKES = "SELECT film_id, user_id FROM film_likes";
    private static final String FIND_LIKES_BY_FILM_ID = "SELECT user_id FROM film_likes WHERE film_id = :filmId";
    private static final String ADD_LIKE = "INSERT INTO film_likes (film_id, user_id) VALUES (:filmId, :userId)";
    private static final String DEL_LIKE = "DELETE FROM film_likes WHERE film_id = :filmId AND user_id = :userId";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = :filmId";

    @Autowired
    private FilmGenresExtractor filmGenresExtractor;

    @Autowired
    private FilmMpaExtractor filmMpaExtractor;

    @Autowired
    FilmLikesExtractor filmLikesExtractor;

    @Autowired
    private FilmExtractor filmExtractor;

    public FilmDbStorage(NamedParameterJdbcOperations jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Метод получения всех имеющихся в коллекции фильмов.
     *
     * @return Коллекция фильмов.
     */
    @Override
    public Collection<Film> findAll() {
        log.info("Film findAll entering");
        Collection<Film> films = findMany(FIND_ALL_QUERY);
        Map<Long, Rating> filmsRating = jdbc.query(FIND_ALL_MPA, filmMpaExtractor);
        Map<Long, HashSet<Long>> filmsLikes = jdbc.query(FIND_ALL_LIKES, filmLikesExtractor);
        Map<Long, LinkedHashSet<Genre>> filmsGenres = jdbc.query(FIND_ALL_FILM_GENRES, filmGenresExtractor);
        films = films.stream()
                .map(film -> {
                    film.setMpa(filmsRating.get(film.getId()));
                    film.setLikesUserId(filmsLikes.get(film.getId()));
                    film.setGenres(filmsGenres.get(film.getId()));
                    return film;
                })
                .collect(Collectors.toList());

        log.info("findAll. films = " + films);

        return films;
    }

    /**
     * Метод получения информации по фильму.
     *
     * @param filmId фильм для обработки.
     * @return Данные по фильму.
     */
    @Override
    public Film findById(long filmId) {
        log.info("Film findById. filmId = " + filmId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", filmId);
        Optional<Film> foundFilm = findOneWithExtractor(FIND_BY_ID_QUERY, params, filmExtractor);
        if (foundFilm.isEmpty()) {
            throw new NotFoundException("Фильм с id = " + filmId + " не найден.");
        }

        List<Long> likesUserId = jdbc.query(FIND_LIKES_BY_FILM_ID, params, new SingleColumnRowMapper<>(Long.class));
        Film film = foundFilm.get();
        film.setLikesUserId(new HashSet<>(likesUserId));

        return film;
    }

    /**
     * Метод добавления нового фильма.
     *
     * @param newFilm добавляемый фильм
     * @return Добавленный фильм.
     */
    @Override
    public Film create(Film newFilm) {
        log.info("Film create. newFilm = " + newFilm);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", newFilm.getName());
        params.addValue("description", newFilm.getDescription());
        params.addValue("releaseDate", newFilm.getReleaseDate());
        params.addValue("duration", newFilm.getDuration());
        params.addValue("mpa", newFilm.getMpa().getId());

        Long id = insert(INSERT_QUERY, params);
        newFilm.setId(id);

        if (newFilm.getGenres() != null && !newFilm.getGenres().isEmpty()) {
            List<HashMap<String, Long>> filmGenres = newFilm.getGenres().stream()
                    .map(genre -> {
                        HashMap<String, Long> genreItem = new HashMap<>();
                        genreItem.put("filmId", id);
                        genreItem.put("genreId", genre.getId());
                        return genreItem;
                    })
                    .collect(Collectors.toList());

            jdbc.batchUpdate(INSERT_GENRES, SqlParameterSourceUtils.createBatch(filmGenres));
        }

        return newFilm;
    }

    /**
     * Метод изменения фильма.
     *
     * @param film фильм с новыми атрибутами.
     * @return Фильм после изменения.
     */
    @Override
    public Film update(Film film) {
        log.info("Film update. film = " + film);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", film.getName());
        params.addValue("description", film.getDescription());
        params.addValue("releaseDate", film.getReleaseDate());
        params.addValue("duration", film.getDuration());
        params.addValue("mpa", film.getMpa().getId());
        params.addValue("filmId", film.getId());

        update(UPDATE_QUERY, params);

        params = new MapSqlParameterSource();
        params.addValue("filmId", film.getId());

        int rowsDeleted = jdbc.update(DELETE_FILM_GENRES, params);
        log.info("Film update, genres deleted. rowsDeleted = " + rowsDeleted);

        if (film.getGenres() != null && !film.getGenres().isEmpty()) {
            List<HashMap<String, Long>> filmGenres = film.getGenres().stream()
                    .map(genre -> {
                        HashMap<String, Long> genreItem = new HashMap<>();
                        genreItem.put("filmId", film.getId());
                        genreItem.put("genreId", genre.getId());
                        return genreItem;
                    })
                    .collect(Collectors.toList());

            jdbc.batchUpdate(INSERT_GENRES, SqlParameterSourceUtils.createBatch(filmGenres));
        }

        return film;
    }

    /**
     * Метод выставления лайка.
     *
     * @param filmId понравившийся фильм.
     * @param userId пользователь, выставляющий лайк.
     * @return Список лайков по фильму.
     */
    @Override
    public List<Long> addLike(long filmId, long userId) {
        log.info("Film addLike. filmId = " + filmId + ", userId = " + userId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", filmId);
        params.addValue("userId", userId);

        jdbc.update(ADD_LIKE, params);

        return jdbc.queryForList(FIND_LIKES_BY_FILM_ID, params, Long.class);
    }

    /**
     * Метод удаления лайка.
     *
     * @param filmId фильм, который перестал нравиться.
     * @param filmId пользователь, удаляющий свой лайк.
     * @return Список лайков по фильму.
     */
    @Override
    public List<Long> deleteLike(long filmId, long userId) {
        log.info("Film deleteLike. filmId = " + filmId + ", userId = " + userId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("filmId", filmId);
        params.addValue("userId", userId);

        jdbc.update(DEL_LIKE, params);

        return jdbc.queryForList(FIND_LIKES_BY_FILM_ID, params, Long.class);
    }

    /**
     * Фильмы по убыванию количества лайков.
     *
     * @param count объем выборки.
     * @return Список фильмов.
     */
    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("getPopularFilms. count = " + count);

        return findAll().stream()
                .sorted((f0, f1) -> -1 * Integer.compare(f0.getLikesUserId() == null ? 0
                        : f0.getLikesUserId().size(), f1.getLikesUserId() == null ? 0 : f1.getLikesUserId().size()))
                .limit(count)
                .collect(Collectors.toList());
    }
}
