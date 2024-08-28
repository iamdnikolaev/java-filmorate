package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.GenreStorage;

import java.util.Collection;

/**
 * Сервис работы с жанрами {@link Genre}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class GenreService {
    private final GenreStorage genreStorage;

    /**
     * Метод получения всех жанров сервиса.
     *
     * @return Коллекция жанров.
     */
    public Collection<Genre> findAll() {
        log.info("Genre findAll.");
        return genreStorage.findAll();
    }

    /**
     * Метод получения данных по конкретному жанру.
     *
     * @param genreId жанр для поиска.
     * @return Данные жанра.
     */
    public Genre findById(long genreId) {
        log.info("Genre findById. genreId = " + genreId);
        return genreStorage.findById(genreId);
    }

    /**
     * Метод добавления жанра.
     *
     * @param newGenre добавляемый жанр.
     * @return Добавленный жанр.
     */
    public Genre create(Genre newGenre) {
        log.info("Genre create. newGenre = " + newGenre);
        return genreStorage.create(newGenre);
    }

    /**
     * Метод изменения жанра.
     *
     * @param genre жанр с новыми атрибутами.
     * @return Жанр после изменения.
     */
    public Genre update(Genre genre) {
        log.info("Genre update. genre = " + genre);
        if (genre.getId() == null || genre.getId() == 0) {
            log.error("Wrong id of the genre.");
            throw new ValidationException("Id должен быть указан.");
        }
        genreStorage.findById(genre.getId());

        return genreStorage.update(genre);
    }
}
