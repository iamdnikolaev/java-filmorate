package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;
import java.util.List;

/**
 * REST-контроллер обслуживания фильмов {@link Film}.
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    /**
     * Поле сервиса для бизнес-логики по фильмам
     */
    private final FilmService filmService;

    /**
     * Обработчик эндпоинта по методу GET с выдачей всех указанных фильмов.
     *
     * @return Коллекция фильмов.
     */
    @GetMapping
    public Collection<Film> findAll() {
        log.info("findAll.");
        return filmService.findAll();
    }

    /**
     * Обработчик эндпоинта по методу GET получения данных по конкретному фильму.
     *
     * @param id фильм для поиска.
     * @return Данные по фильму.
     */
    @GetMapping("/{id}")
    public Film findById(@PathVariable long id) {
        return filmService.findById(id);
    }

    /**
     * Обработчик эндпоинта по методу POST для добавления нового фильма.
     *
     * @return Добавленный фильм.
     */
    @PostMapping
    public Film create(@Valid @RequestBody Film newFilm) {
        log.info("Film create. newFilm = " + newFilm);
        return filmService.create(newFilm);
    }

    /**
     * Обработчик эндпоинта по методу PUT для изменения фильма.
     *
     * @return Фильм после изменения.
     */
    @PutMapping
    public Film update(@Valid @RequestBody Film film) {
        log.info("Film update. film = " + film);
        return filmService.update(film);
    }

    /**
     * Обработчик эндпоинта по методу PUT для добавления лайка.
     *
     * @param id     фильм, которому добавляется лайк.
     * @param userId пользователь, выставляющий лайк.
     * @return Список лайков фильма с id пользователей.
     */
    @PutMapping("/{id}/like/{userId}")
    public List<Long> addLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Film addLike. id = " + id + ", userId = " + userId);
        return filmService.addLike(id, userId);
    }

    /**
     * Обработчик эндпоинта по методу DELETE для удаления лайка.
     *
     * @param id     фильм, у которого удаляется лайк.
     * @param userId пользователь, убирающий своё мнение.
     * @return Список лайков фильма с id пользователей.
     */
    @DeleteMapping("/{id}/like/{userId}")
    public List<Long> deleteLike(@PathVariable long id, @PathVariable long userId) {
        log.info("Film deleteLike. id = " + id + ", userId = " + userId);
        return filmService.deleteLike(id, userId);
    }

    /**
     * Обработчик эндпоинта по методу GET для получения списка фильмов по количеству лайков.
     *
     * @param count объем выборки (по умолчанию 10).
     * @return Список фильмов.
     */
    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        log.info("getPopularFilms. count = " + count);
        return filmService.getPopularFilms(count);
    }
}
