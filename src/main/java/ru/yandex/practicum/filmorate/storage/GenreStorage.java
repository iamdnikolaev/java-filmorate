package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;

public interface GenreStorage {
    Collection<Genre> findAll();

    Genre findById(long genreId);

    Genre create(Genre newGenre);

    Genre update(Genre genre);
}
