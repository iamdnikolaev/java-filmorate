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
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * REST-контроллер работы с пользователями {@link User}
 *
 * @version 1.0
 * @author Николаев Д.В.
 */
@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    /**
     * Хранилище пользователей.
     */
    private final Map<Long, User> users = new HashMap<>();

    /**
     * Обработчик эндпоинта по методу GET с выдачей всех пользователей сервиса.
     *
     * @return Коллекция пользователей.
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("findAll. users = " + users);
        return users.values();
    }

    /**
     * Обработчик эндпоинта по методу POST для добавления пользователя.
     *
     * @return Добавленный пользователь.
     */
    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        isLoginUsed(newUser.getLogin(), 0);

        fillEmptyName(newUser, "create");

        newUser.setId(getNextId());
        log.info("User create. newUser = " + newUser);
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    /**
     * Обработчик эндпоинта по методу PUT для изменения пользователя.
     *
     * @return Пользователь после изменения.
     */
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        if (user.getId() == null || user.getId() == 0) {
            log.error("User update. Wrong id, user = " + user);
            throw new ValidationException("Id должен быть указан.");
        }
        if (users.containsKey(user.getId())) {
            isLoginUsed(user.getLogin(), user.getId());

            User oldUser = users.get(user.getId());

            oldUser.setLogin(user.getLogin());
            fillEmptyName(user, "update");
            oldUser.setEmail(user.getEmail());
            oldUser.setName(user.getName());
            oldUser.setBirthday(user.getBirthday());

            return oldUser;
        }
        log.error("User update. User is not found by id. " + user);
        throw new NotFoundException("Пользователь с id = " + user.getId() + " не найден.");
    }

    /**
     * Метод для генерации уникальных идентификаторов.
     *
     * @return Новый id выше максимального из имеющихся.
     */
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        log.debug("getNextId. currentMaxId = " + currentMaxId);
        return ++currentMaxId;
    }

    /** Метод проверки наличия уже созданного пользователя в хранилище по логину.
     * @param loginToFind логин для поиска.
     * @param excludeId идентификатор пользователя, исключаемый из результатов поиска.
     */
    private void isLoginUsed(String loginToFind, long excludeId) throws ValidationException {
        if (loginToFind != null && !loginToFind.isBlank()) {
            Optional<User> userWithLogin = users.values()
                    .stream()
                    .filter(user -> user.getId() != excludeId)
                    .filter(user -> user.getLogin().equals(loginToFind))
                    .findAny();
            if (userWithLogin.isPresent()) {
                log.error("isLoginUsed. Login \"" + loginToFind + "\" is already used. excludeId = " + excludeId);
                throw new ValidationException("Этот логин уже используется.");
            }
        }
    }

    /**
     * Метод заполнения пустого имени пользователя логином.
     * @param user проверяемый пользователь
     * @param actionName имя операции для записи в лог
     */
    private void fillEmptyName(User user, String actionName) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("User " + actionName + " . Filling empty name by login.");
            user.setName(user.getLogin());
        }
    }
}