package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.HashSet;
import java.util.LinkedHashSet;

@Component
public class FilmExtractor implements ResultSetExtractor<Film> {
    @Override
    public Film extractData(ResultSet rs)
            throws SQLException, DataAccessException {
        Film film = null;
        while (rs.next()) {
            if (film == null) {
                film = Film.builder()
                        .id(rs.getLong("film_id"))
                        .name(rs.getString("film_name"))
                        .description(rs.getString("description"))
                        .duration(rs.getInt("duration"))
                        .likesUserId(new HashSet<>())
                        .genres(new LinkedHashSet<>())
                        .build();
            }

            Timestamp releaseDate = rs.getTimestamp("release_date");
            film.setReleaseDate(releaseDate.toLocalDateTime().toLocalDate());

            long rating_id = rs.getLong("rating_id");
            if (rating_id > 0 && film.getMpa() == null) {
                film.setMpa(new Rating(rating_id, rs.getString("rating_name")));
            }

            long genre_id = rs.getLong("genre_id");
            if (genre_id > 0) {
                Genre genre = new Genre(genre_id, rs.getString("genre_name"));
                film.getGenres().add(genre);
            }
        }
        return film;
    }
}
