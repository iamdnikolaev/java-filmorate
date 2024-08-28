package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class GenreDbStorage extends BaseRepository implements GenreStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String INSERT_QUERY = "INSERT INTO genres (name) VALUES (:name)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE genre_id = :genreId";
    private static final String UPDATE_QUERY = "UPDATE genres SET name = :name WHERE genre_id = :genreId";

    public GenreDbStorage(NamedParameterJdbcOperations jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Метод получения всех жанров сервиса.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
     *
     * @return Коллекция жанров.
     */
    @Override
    public Collection<Genre> findAll() {
        Collection<Genre> genres = findMany(FIND_ALL_QUERY);

        return genres;
    }

    /**
     * Метод получения данных по конкретному рейтингу.
     *
     * @param genreId жанр для поиска.
     * @return Данные жанра.
     */
    @Override
    public Genre findById(long genreId) {
        log.info("Genre findById. genreId = " + genreId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("genreId", genreId);
        Optional<Genre> foundGenre = findOne(FIND_BY_ID_QUERY, params);
        if (foundGenre.isEmpty()) {
            throw new NotFoundException("Жанр с id = " + genreId + " не найден.");
        }

        return foundGenre.get();
    }

    /**
     * Метод добавления жанра.
     *
     * @param newGenre добавляемый жанр.
     * @return Добавленный жанр.
     */
    @Override
    public Genre create(Genre newGenre) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", newGenre.getName());

        long id = insert(INSERT_QUERY, params);
        newGenre.setId(id);

        return newGenre;
    }

    /**
     * Метод изменения жанра.
     *
     * @param genre жанр с новыми атрибутами.
     * @return Жанр после изменения.
     */
    @Override
    public Genre update(Genre genre) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", genre.getName());
        params.addValue("genreId", genre.getId());

        update(UPDATE_QUERY, params);

        return genre;
    }
}
