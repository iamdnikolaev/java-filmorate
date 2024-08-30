package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.service.GenreService;

import java.util.Collection;

/**
 * REST-контроллер работы с жанрами {@link Genre}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@RestController
@RequestMapping("/genres")
@Slf4j
@RequiredArgsConstructor
public class GenreController {
    /**
     * Поле сервиса для бизнес-логики по жанрам
     */
    private final GenreService genreService;

    /**
     * Обработчик эндпоинта по методу POST для добавления жанра.
     *
     * @return Добавленный жанр.
     */
    @PostMapping
    public Genre create(@Valid @RequestBody Genre newGenre) {
        log.info("Genre create. newGenre = " + newGenre);
        return genreService.create(newGenre);
    }

    /**
     * Обработчик эндпоинта по методу GET с выдачей всех жанров сервиса.
     *
     * @return Коллекция жанров.
     */
    @GetMapping
    public Collection<Genre> findAll() {
        log.info("findAll.");
        return genreService.findAll();
    }

    /**
     * Обработчик эндпоинта по методу GET для получения данных по конкретному жанру.
     *
     * @param id жанр для поиска.
     * @return Данные жанра.
     */
    @GetMapping("/{id}")
    public Genre findById(@PathVariable long id) {
        return genreService.findById(id);
    }

    /**
     * Обработчик эндпоинта по методу PUT для изменения жанра.
     *
     * @return Жанр после изменения.
     */
    @PutMapping
    public Genre update(@Valid @RequestBody Genre genre) {
        log.info("Genre update. genre = " + genre);
        return genreService.update(genre);
    }
}
