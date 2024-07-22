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
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;
import java.util.List;

/**
 * REST-контроллер работы с пользователями {@link User}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    /**
     * Поле сервиса для бизнес-логики по пользователям
     */
    private final UserService userService;

    /**
     * Обработчик эндпоинта по методу POST для добавления пользователя.
     *
     * @return Добавленный пользователь.
     */
    @PostMapping
    public User create(@Valid @RequestBody User newUser) {
        log.info("User create. newUser = " + newUser);
        return userService.create(newUser);
    }

    /**
     * Обработчик эндпоинта по методу GET с выдачей всех пользователей сервиса.
     *
     * @return Коллекция пользователей.
     */
    @GetMapping
    public Collection<User> findAll() {
        log.info("findAll.");
        return userService.findAll();
    }

    /**
     * Обработчик эндпоинта по методу GET для получения данных по конкретному пользователю.
     *
     * @param id пользователь для поиска.
     * @return Данные пользователя.
     */
    @GetMapping("/{id}")
    public User findById(@PathVariable long id) {
        return userService.findById(id);
    }

    /**
     * Обработчик эндпоинта по методу PUT для изменения пользователя.
     *
     * @return Пользователь после изменения.
     */
    @PutMapping
    public User update(@Valid @RequestBody User user) {
        log.info("User update. user = " + user);
        return userService.update(user);
    }

    /**
     * Обработчик эндпоинта по методу PUT для добавления в друзья.
     *
     * @param id       пользователь, которому добавляется друг.
     * @param friendId пользователь, добавляемый в друзья.
     * @return Список друзей пользователя id.
     */
    @PutMapping("/{id}/friends/{friendId}")
    public List<Long> addFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("User addFriend. id = " + id + ", friendId = " + friendId);
        return userService.addFriend(id, friendId);
    }

    /**
     * Обработчик эндпоинта по методу DELETE для удаления из друзей.
     *
     * @param id       пользователь, который лишается друга.
     * @param friendId пользователь, убираемый из друзей.
     * @return Список друзей пользователя id.
     */
    @DeleteMapping("/{id}/friends/{friendId}")
    public List<Long> deleteFriend(@PathVariable long id, @PathVariable long friendId) {
        log.info("User deleteFriend. id = " + id + ", friendId = " + friendId);
        return userService.deleteFriend(id, friendId);
    }

    /**
     * Обработчик эндпоинта по методу GET для получения всех друзей пользователя.
     *
     * @param id пользователь для обработки.
     * @return Список друзей пользователя.
     */
    @GetMapping("/{id}/friends")
    public List<User> getAllFriends(@PathVariable long id) {
        log.info("User getAllFriends. id = " + id);
        return userService.getAllFriends(id);
    }

    /**
     * Обработчик эндпоинта по методу GET для получения друзей, общих у двух пользователей.
     *
     * @param id      пользователь для обработки.
     * @param otherId пользователь для обработки.
     * @return Список общих друзей пользователей.
     */
    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable long id, @PathVariable long otherId) {
        log.info("User getAllFriends. id = " + id);
        return userService.getCommonFriends(id, otherId);
    }
}