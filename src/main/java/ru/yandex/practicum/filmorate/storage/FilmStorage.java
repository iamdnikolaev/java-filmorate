package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {
    Collection<Film> findAll();

    Film findById(long filmId);

    Film create(Film newFilm);

    Film update(Film film);

    List<Long> addLike(long filmId, long userId);

    List<Long> deleteLike(long filmId, long userId);

    List<Film> getPopularFilms(int count);
}
