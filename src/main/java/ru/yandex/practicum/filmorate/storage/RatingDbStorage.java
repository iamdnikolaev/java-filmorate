package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

@Repository
@Slf4j
public class RatingDbStorage extends BaseRepository implements RatingStorage {
    private static final String FIND_ALL_QUERY = "SELECT * FROM ratings";
    private static final String INSERT_QUERY = "INSERT INTO ratings (name) VALUES (:name)";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM ratings WHERE rating_id = :ratingId";
    private static final String UPDATE_QUERY = "UPDATE ratings SET name = :name WHERE rating_id = :ratingId";

    public RatingDbStorage(NamedParameterJdbcOperations jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Метод получения всех рейтингов сервиса.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                        eeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeeee
     *
     * @return Коллекция рейтингов.
     */
    @Override
    public Collection<Rating> findAll() {
        Collection<Rating> ratings = findMany(FIND_ALL_QUERY);

        return ratings;
    }

    /**
     * Метод получения данных по конкретному рейтингу.
     *
     * @param ratingId рейтинг для поиска.
     * @return Данные рейтинга.
     */
    @Override
    public Rating findById(long ratingId) {
        log.info("Rating findById. ratingId = " + ratingId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("ratingId", ratingId);
        Optional<Rating> foundRating = findOne(FIND_BY_ID_QUERY, params);
        if (foundRating.isEmpty()) {
            throw new NotFoundException("Рейтинг с id = " + ratingId + " не найден.");
        }

        return foundRating.get();
    }

    /**
     * Метод добавления рейтинга.
     *
     * @param newRating добавляемый рейтинг.
     * @return Добавленный рейтинг.
     */
    @Override
    public Rating create(Rating newRating) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", newRating.getName());

        long id = insert(INSERT_QUERY, params);
        newRating.setId(id);

        return newRating;
    }

    /**
     * Метод изменения рейтинга.
     *
     * @param rating рейтинг с новыми атрибутами.
     * @return Рейтинг после изменения.
     */
    @Override
    public Rating update(Rating rating) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("name", rating.getName());
        params.addValue("ratingId", rating.getId());

        update(UPDATE_QUERY, params);

        return rating;
    }
}
