package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Genre;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

@Component
public class FilmGenresExtractor implements ResultSetExtractor<Map<Long, LinkedHashSet<Genre>>> {
    @Override
    public Map<Long, LinkedHashSet<Genre>> extractData(ResultSet rs)
            throws SQLException, DataAccessException {
        Map<Long, LinkedHashSet<Genre>> data = new HashMap<>();
        while (rs.next()) {
            Long filmId = rs.getLong("film_id");
            data.putIfAbsent(filmId, new LinkedHashSet<>());
            Genre genre = new Genre(0L, "");
            genre.setId(rs.getLong("genre_id"));
            genre.setName(rs.getString("genre_name"));
            data.get(filmId).add(genre);
        }
        return data;
    }
}
