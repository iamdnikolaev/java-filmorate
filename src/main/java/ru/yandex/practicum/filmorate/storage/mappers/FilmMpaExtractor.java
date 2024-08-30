package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@Component
public class FilmMpaExtractor implements ResultSetExtractor<Map<Long, Rating>> {
    @Override
    public Map<Long, Rating> extractData(ResultSet rs)
            throws SQLException, DataAccessException {
        Map<Long, Rating> data = new HashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            data.putIfAbsent(filmId, null);
            Rating mpa = new Rating(0L, "");
            mpa.setId(rs.getLong("mpa"));
            mpa.setName(rs.getString("mpa_name"));
            data.put(filmId, mpa);
        }
        return data;
    }
}