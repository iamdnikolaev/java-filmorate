package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class BaseRepository<T> {
    protected final NamedParameterJdbcOperations jdbc;
    protected final RowMapper<T> mapper;

    protected Optional<T> findOne(String query, MapSqlParameterSource params) {
        try {
            List<T> result = jdbc.query(query, params, mapper);
            if (result.isEmpty()) {
                return Optional.empty();
            } else {
                return Optional.ofNullable(result.get(0));
            }
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected Optional<T> findOneWithExtractor(String query, MapSqlParameterSource params, ResultSetExtractor<T> extractor) {
        try {
            T result = jdbc.query(query, params, extractor);
            if (result == null) {
                return Optional.empty();
            } else {
                return Optional.of(result);
            }
        } catch (EmptyResultDataAccessException ignored) {
            return Optional.empty();
        }
    }

    protected Collection<T> findMany(String query) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        return jdbc.query(query, params, mapper);
    }

    protected void update(String query, MapSqlParameterSource params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            throw new NotFoundException("Не удалось обновить данные");
        }
    }

    protected long insert(String query, MapSqlParameterSource params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(query, params, keyHolder);
        Long id = keyHolder.getKeyAs(Long.class);

        if (id != null) {
            return id;
        } else {
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }
}