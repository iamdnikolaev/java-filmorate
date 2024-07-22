package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация интерфейса работы с фильмами с хранением в памяти.
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    /**
     * Коллекция фильмов.
     */
    private final Map<Long, Film> films = new HashMap<>();

    /**
     * Метод получения всех имеющихся в коллекции фильмов.
     *
     * @return Коллекция фильмов.
     */
    @Override
    public Collection<Film> findAll() {
        log.info("findAll. films = " + films);
        return films.values();
    }

    /**
     * Метод получения информации по фильму.
     *
     * @param filmId фильм для обработки.
     * @return Данные по фильму.
     */
    @Override
    public Film findById(long filmId) {
        log.info("Film findById. filmId = " + filmId);
        return films.values().stream()
                .filter(x -> x.getId() == filmId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Фильм с id = " + filmId + " не найден."));
    }

    /**
     * Метод добавления нового фильма.
     *
     * @param newFilm добавляемый фильм
     * @return Добавленный фильм.
     */
    @Override
    public Film create(Film newFilm) {
        newFilm.setId(getNextId());
        log.info("Film create. newFilm = " + newFilm);
        films.put(newFilm.getId(), newFilm);
        return newFilm;
    }

    /**
     * Метод изменения фильма.
     *
     * @param film фильм с новыми атрибутами.
     * @return Фильм после изменения.
     */
    @Override
    public Film update(Film film) {
        Film oldFilm = films.get(film.getId());
        log.info("Film update. film = " + film);
        oldFilm.setName(film.getName());
        oldFilm.setDescription(film.getDescription());
        oldFilm.setReleaseDate(film.getReleaseDate());
        oldFilm.setDuration(film.getDuration());

        return oldFilm;
    }

    /**
     * Метод выставления лайка.
     *
     * @param filmId понравившийся фильм.
     * @param userId пользователь, выставляющий лайк.
     * @return Список лайков по фильму.
     */
    @Override
    public List<Long> addLike(long filmId, long userId) {
        log.info("Film addLike. filmId = " + filmId + ", userId = " + userId);
        Film film = films.get(filmId);
        Set<Long> likes = film.getLikesUserId();
        if (likes == null) {
            likes = new HashSet<>();
            film.setLikesUserId(likes);
        }
        likes.add(userId);

        return likes.stream().toList();
    }

    /**
     * Метод удаления лайка.
     *
     * @param filmId фильм, который перестал нравится.
     * @param userId пользователь, удаляющий свой лайк.
     * @return Список лайков по фильму.
     */
    @Override
    public List<Long> deleteLike(long filmId, long userId) {
        log.info("Film deleteLike. filmId = " + filmId + ", userId = " + userId);
        Film film = films.get(filmId);
        Set<Long> likes = film.getLikesUserId();
        if (likes != null && likes.contains(userId)) {
            likes.remove(userId);
        }

        return likes.stream().toList();
    }

    /**
     * Фильмы по убыванию количества лайков.
     *
     * @param count объем выборки.
     * @return Список фильмов.
     */
    @Override
    public List<Film> getPopularFilms(int count) {
        log.info("getPopularFilms. count = " + count);
        return films.values()
                .stream()
                .sorted((f0, f1) -> -1 * Integer.valueOf(f0.getLikesUserId() == null ? 0 : f0.getLikesUserId().size())
                        .compareTo(Integer.valueOf(f1.getLikesUserId() == null ? 0 : f1.getLikesUserId().size())))
                .limit(count)
                .collect(Collectors.toList());
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
}
