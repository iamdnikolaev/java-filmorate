package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.GenreStorage;
import ru.yandex.practicum.filmorate.storage.RatingStorage;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Сервис работы с фильмами {@link Film}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {
    /**
     * Константа для проверки даты релиза - появление первого в мире фильма.
     */
    public static final LocalDate DATE_OF_CINEMA = LocalDate.of(1895, 12, 28);

    /**
     * Поле репозитория фильмов.
     */
    private final FilmStorage filmStorage;

    /**
     * Поле репозитория жанров.
     */
    private final GenreStorage genreStorage;

    /**
     * Поле репозитория рейтингов.
     */
    private final RatingStorage ratingStorage;

    /**
     * Поле сервиса по работе с пользователями.
     */
    private final UserService userService;

    /**
     * Метод получения всех имеющихся в коллекции фильмов.
     *
     * @return Коллекция фильмов.
     */
    public Collection<Film> findAll() {
        log.info("findAll.");
        return filmStorage.findAll();
    }

    /**
     * Метод получения данных по конкретному фильму.
     *
     * @param filmId фильм для поиска.
     * @return Данные по фильму.
     */
    public Film findById(long filmId) {
        log.info("Film findById. filmId = " + filmId);
        return filmStorage.findById(filmId);
    }

    /**
     * Метод добавления нового фильма.
     *
     * @param newFilm добавляемый фильм
     * @return Добавленный фильм.
     */
    public Film create(Film newFilm) {
        isReleaseDateTooOld(newFilm, "create");
        //isFilmExists(newFilm.getName(), newFilm.getReleaseDate(), 0L); отключено, в тестах postman'а допустимы дубли
        isRatingExists(newFilm.getMpa().getId());
        isGenresExists(newFilm.getGenres());

        log.info("Film create. newFilm = " + newFilm);
        return filmStorage.create(newFilm);
    }

    /**
     * Метод изменения фильма.
     *
     * @param film фильм с новыми атрибутами.
     * @return Фильм после изменения.
     */
    public Film update(Film film) {
        if (film.getId() == null || film.getId() == 0) {
            log.error("Film update. Wrong id, film = " + film);
            throw new ValidationException("Id должен быть указан.");
        }
        isReleaseDateTooOld(film, "update");
        isRatingExists(film.getMpa().getId());
        isGenresExists(film.getGenres());

        Collection<Film> films = filmStorage.findAll();
        Optional<Film> theFilm = films
                .stream()
                .filter(f -> f.getId().equals(film.getId()))
                .findFirst();

        if (theFilm.isPresent()) {
            //isFilmExists(film.getName(), film.getReleaseDate(), film.getId());

            return filmStorage.update(film);
        }
        log.error("Film update. Film is not found by id. " + film);
        throw new NotFoundException("Фильм с id = " + film.getId() + " не найден.");
    }

    /**
     * Метод выставления лайка.
     *
     * @param filmId понравившийся фильм.
     * @param userId пользователь, выставляющий лайк.
     * @return Список лайков по фильму.
     */
    public List<Long> addLike(long filmId, long userId) {
        log.info("Film addLike. filmId = " + filmId + ", userId = " + userId);

        checkFilmId(filmId);
        userService.checkUserId(userId, "userId");

        return filmStorage.addLike(filmId, userId);
    }

    /**
     * Метод удаления лайка.
     *
     * @param filmId фильм, который перестал нравится.
     * @param userId пользователь, удаляющий свой лайк.
     * @return Список лайков по фильму.
     */
    public List<Long> deleteLike(long filmId, long userId) {
        log.info("Film deleteLike. filmId = " + filmId + ", userId = " + userId);

        checkFilmId(filmId);
        userService.checkUserId(userId, "userId");

        return filmStorage.deleteLike(filmId, userId);
    }

    /**
     * Фильмы по убыванию количества лайков.
     *
     * @param count объем выборки.
     * @return Список фильмов.
     */
    public List<Film> getPopularFilms(int count) {
        log.info("getPopularFilms. count = " + count);

        return filmStorage.getPopularFilms(count);
    }

    /**
     * Метод проверки наличия указанного фильма в хранилище по id.
     *
     * @param filmId проверяемый идентификатор фильма.
     */
    private void checkFilmId(long filmId) {
        log.info("checkFilmId. filmId = " + filmId);

        if (filmId == 0) {
            log.error("Wrong filmId");
            throw new ValidationException("filmId должен быть указан.");
        }
        filmStorage.findById(filmId);
    }

    /**
     * Метод проверки наличия уже указанного фильма в коллекции по названию и дате выхода.
     *
     * @param nameToFind        название для поиска.
     * @param releaseDateToFind дата релиза для поиска.
     * @param excludeId         идентификатор фильма, исключаемый из результатов поиска.
     */
    private void isFilmExists(String nameToFind, LocalDate releaseDateToFind, long excludeId)
            throws ValidationException {
        if (nameToFind != null && !nameToFind.isBlank()) {
            Collection<Film> films = filmStorage.findAll();
            Optional<Film> filmExists = films
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
     * Метод проверки наличия указанного рейтинга в репозитории.
     *
     * @param idToFind id рейтинга для поиска
     */
    private void isRatingExists(long idToFind)
            throws ValidationException {
        if (idToFind > 0) {
            Collection<Rating> ratings = ratingStorage.findAll();
            Optional<Rating> ratingExists = ratings
                    .stream()
                    .filter(rating -> rating.getId() == idToFind)
                    .findAny();
            if (!ratingExists.isPresent()) {
                log.error("isRatingExists. Rating with id = \"" + idToFind + " does not exist.");
                throw new ValidationException("Этот указанный рейтинг не существует.");
            }
        }
    }

    /**
     * Метод проверки наличия указанных жанров в репозитории.
     *
     * @param genresToFind множество жанров для проверки на существование
     */
    private void isGenresExists(Set<Genre> genresToFind)
            throws ValidationException {
        if (genresToFind != null && !genresToFind.isEmpty()) {
            Collection<Genre> genresAll = genreStorage.findAll();
            Optional<Genre> genreNotExists = genresToFind
                    .stream()
                    .filter(genreToFind -> !genresAll.contains(genreToFind))
                    .findAny();

            if (genreNotExists.isPresent()) {
                log.error("isGenresExists. Genre \"" + genreNotExists + " does not exist.");
                throw new ValidationException("Указанный жанр с id = " + genreNotExists.get().getId() + " не существует.");
            }
        }
    }

    /**
     * Метод проверки даты выхода фильма, что она не ранее выхода самого первого
     * фильма {@link FilmService#DATE_OF_CINEMA}.
     *
     * @param film       проверяемый фильм
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
