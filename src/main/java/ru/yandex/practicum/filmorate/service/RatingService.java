package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.util.Collection;

/**
 * Сервис работы с рейтингами {@link Rating}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RatingService {
    private final RatingStorage ratingStorage;

    /**
     * Метод получения всех рейтингов сервиса.
     *
     * @return Коллекция рейтингов.
     */
    public Collection<Rating> findAll() {
        log.info("Rating findAll.");
        return ratingStorage.findAll();
    }

    /**
     * Метод получения данных по конкретному рейтингу.
     *
     * @param ratingId рейтинг для поиска.
     * @return Данные рейтинга.
     */
    public Rating findById(long ratingId) {
        log.info("Rating findById. ratingId = " + ratingId);
        return ratingStorage.findById(ratingId);
    }

    /**
     * Метод добавления рейтинга.
     *
     * @param newRating добавляемый рейтинг.
     * @return Добавленный рейтинг.
     */
    public Rating create(Rating newRating) {
        log.info("Rating create. newRating = " + newRating);
        return ratingStorage.create(newRating);
    }

    /**
     * Метод изменения рейтинга.
     *
     * @param rating рейтинг с новыми атрибутами.
     * @return Рейтинг после изменения.
     */
    public Rating update(Rating rating) {
        log.info("Rating update. rating = " + rating);
        if (rating.getId() == null || rating.getId() == 0) {
            log.error("Wrong id of the rating.");
            throw new ValidationException("Id должен быть указан.");
        }
        ratingStorage.findById(rating.getId());

        return ratingStorage.update(rating);
    }
}
