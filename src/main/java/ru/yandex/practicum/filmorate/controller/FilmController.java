package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST-контроллер обслуживания фильмов {@link Film}.
 *
 * @version 1.0
 * @author Николаев Д.В.
 */
@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    /**
     * Константа для проверки даты релиза - появление первого фильма в мире.
     */
    public static final LocalDate DATE_OF_CINEMA = LocalDate.of(1895, 12, 28);

    /**
     * Коллекция фильмов.
     */
    private final Map<Long, Film> films = new HashMap<>();

    /**
     * Обработчик эндпоинта по методу GET с выдачей всех указанных фильмов.
     *
     * @return Коллекция фильмов.
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("findAll. films = " + films);
        return films.values();
    }

    /**
     * Обработчик эндпоинта по методу POST для добавления нового фильма.
     *
     * @return Добавленный фильм.
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        isReleaseDateTooOld(newFilm, "create");
        isFilmExists(newFilm.getName(), newFilm.getReleaseDate(), 0L);

        newFilm.setId(getNextId());
        log.info("Film create. newFilm = " + newFilm);
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    /**
     * Обработчик эндпоинта по методу PUT для изменения фильма.
     *
     * @return Фильм после изменения.
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        if (film.getId() == null || film.getId() == 0) {
            log.error("Film update. Wrong id, film = " + film);
            throw new ValidationException("Id должен быть указан.");
        }
        isReleaseDateTooOld(film, "update");

        if (films.containsKey(film.getId())) {
            isFilmExists(film.getName(), film.getReleaseDate(), film.getId());

            Film oldFilm = films.get(film.getId());

            oldFilm.setName(film.getName());
            oldFilm.setDescription(film.getDescription());
            oldFilm.setReleaseDate(film.getReleaseDate());
            oldFilm.setDuration(film.getDuration());

            return oldFilm;
        }
        log.error("Film update. Film is not found by id. " + film);
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
    }

    /**
     * Метод для генерации уникальных идентификаторов.
     *
     * @return Новый id выше максимального из имеющихся.
     */
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("getNextId. currentMaxId = " + currentMaxId);
        return ++currentMaxId;
    }

    /** Метод проверки наличия уже указанного фильма в коллекции по названию и дате выхода.
     * @param nameToFind название для поиска.
     * @param releaseDateToFind дата релиза для поиска.
     * @param excludeId идентификатор фильма, исключаемый из результатов поиска.
     */
    private void isFilmExists(String nameToFind, LocalDate releaseDateToFind, long excludeId)
            throws ValidationException {
        if (nameToFind != null && !nameToFind.isBlank()) {
            Optional<Film> filmExists = films.values()
                    .stream()
                    .filter(film -> film.getId() != excludeId)
                    .filter(film -> film.getName().equals(nameToFind))
                    .filter(film -> film.getReleaseDate().equals(releaseDateToFind))
                    .findAny();
            if (filmExists.isPresent()) {
                log.error("isFilmExists. Film \"" + nameToFind + "\" with release date " + releaseDateToFind +
                        " already exists. excludeId = " + excludeId);
                throw new ValidationException("Этот фильм уже указан.");
            }
        }
    }

    /**
     * Метод проверки даты выхода фильма, что она не ранее выхода самого первого
     * фильма {@link FilmController#DATE_OF_CINEMA}.
     * @param film проверяемый фильм
     * @param actionName имя операции для записи в лог
     * @throws ValidationException
     */
    private void isReleaseDateTooOld(Film film, String actionName) throws ValidationException {
        if (film.getReleaseDate().isBefore(DATE_OF_CINEMA)) {
            log.error("Film " + actionName + ". Wrong release date. film = " + film);
            throw new ValidationException("Дата релиза не может быть ранее " + DATE_OF_CINEMA);
        }
    }
}
