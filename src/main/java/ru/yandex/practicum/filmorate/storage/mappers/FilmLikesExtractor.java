package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

@Component
public class FilmLikesExtractor implements ResultSetExtractor<Map<Long, HashSet<Long>>> {
    @Override
    public Map<Long, HashSet<Long>> extractData(ResultSet rs)
            throws SQLException, DataAccessException {
        Map<Long, HashSet<Long>> data = new HashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            data.putIfAbsent(filmId, new HashSet<>());
            data.get(filmId).add(rs.getLong("user_id"));
        }
        return data;
    }
}
