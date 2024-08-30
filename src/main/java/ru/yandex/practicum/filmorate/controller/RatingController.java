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
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.RatingService;

import java.util.Collection;

/**
 * REST-контроллер работы с рейтингами {@link Rating}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class RatingController {
    /**
     * Поле сервиса для бизнес-логики по рейтингам
     */
    private final RatingService ratingService;

    /**
     * Обработчик эндпоинта по методу POST для добавления рейтинга.
     *
     * @return Добавленный рейтинг.
     */
    @PostMapping
    public Rating create(@Valid @RequestBody Rating newRating) {
        log.info("Rating create. newRating = " + newRating);
        return ratingService.create(newRating);
    }

    /**
     * Обработчик эндпоинта по методу GET с выдачей всех рейтингов сервиса.
     *
     * @return Коллекция рейтингов.
     */
    @GetMapping
    public Collection<Rating> findAll() {
        log.info("findAll.");
        return ratingService.findAll();
    }

    /**
     * Обработчик эндпоинта по методу GET для получения данных по конкретному рейтингу.
     *
     * @param id рейтинг для поиска.
     * @return Данные рейтинга.
     */
    @GetMapping("/{id}")
    public Rating findById(@PathVariable long id) {
        return ratingService.findById(id);
    }

    /**
     * Обработчик эндпоинта по методу PUT для изменения рейтинга.
     *
     * @return Рейтинг после изменения.
     */
    @PutMapping
    public Rating update(@Valid @RequestBody Rating rating) {
        log.info("Rating update. rating = " + rating);
        return ratingService.update(rating);
    }
}
