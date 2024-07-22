package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Сервис работы с пользователями {@link User}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {
    private final UserStorage userStorage;

    /**
     * Метод получения всех пользователей сервиса.
     *
     * @return Коллекция пользователей.
     */
    public Collection<User> findAll() {
        log.info("User findAll.");
        return userStorage.findAll();
    }

    /**
     * Метод получения данных по конкретному пользователю.
     *
     * @param userId пользователь для поиска.
     * @return Данные пользователя.
     */
    public User findById(long userId) {
        log.info("User findById. userId = " + userId);
        return userStorage.findById(userId);
    }

    /**
     * Метод добавления пользователя.
     *
     * @param newUser добавляемый пользователь.
     * @return Добавленный пользователь.
     */
    public User create(User newUser) {
        isLoginUsed(newUser.getLogin(), 0);

        log.info("User create. newUser = " + newUser);
        return userStorage.create(newUser);
    }

    /**
     * Метод изменения пользователя.
     *
     * @param user пользователь с новыми атрибутами.
     * @return Пользователь после изменения.
     */
    public User update(User user) {
        log.info("User update. user = " + user);
        if (user.getId() == null || user.getId() == 0) {
            log.error("Wrong id of the user.");
            throw new ValidationException("Id должен быть указан.");
        }
        userStorage.findById(user.getId());
        isLoginUsed(user.getLogin(), user.getId());

        return userStorage.update(user);
    }

    /**
     * Метод добавления в друзья.
     *
     * @param userId   пользователь, которому добавляется друг.
     * @param friendId пользователь, добавляемый в друзья.
     * @return Список друзей пользователя userId.
     */
    public List<Long> addFriend(long userId, long friendId) {
        log.info("User addFriend. userId = " + userId + ", friendId = " + friendId);
        checkUserId(userId, "userId");
        checkUserId(friendId, "friendId");
        if (userId == friendId) {
            log.error("Wrong friendId.");
            throw new ValidationException("Самого себя в друзья не добавляем.");
        }

        return userStorage.addFriend(userId, friendId, false);
    }

    /**
     * Метод удаления из друзей
     *
     * @param userId   пользователь, который лишается друга.
     * @param friendId пользователь, убираемый из друзей.
     * @return Список друзей пользователя userId.
     */
    public List<Long> deleteFriend(long userId, long friendId) {
        log.info("User deleteFriend. userId = " + userId + ", friendId = " + friendId);
        checkUserId(userId, "userId");
        checkUserId(friendId, "friendId");

        return userStorage.deleteFriend(userId, friendId, false);
    }

    /**
     * Метод получения всех друзей пользователя.
     *
     * @param userId пользователь, по которому необходимо вывести друзей.
     * @return Список друзей пользователя.
     */
    public List<User> getAllFriends(long userId) {
        log.info("User getAllFriends. userId = " + userId);
        checkUserId(userId, "userId");

        return userStorage.getAllFriends(userId);
    }

    /**
     * Метод получения друзей пользователя, общих с другим пользователем.
     *
     * @param userId  пользователь, по которому необходимо вывести друзей.
     * @param otherId другой пользователь, для поиска общих друзей с ним.
     * @return Список общих друзей.
     */
    public List<User> getCommonFriends(long userId, long otherId) {
        log.info("User getCommonFriends. userId = " + userId + ", otherId = " + otherId);
        checkUserId(userId, "userId");
        checkUserId(otherId, "otherId");

        return userStorage.getCommonFriends(userId, otherId);
    }

    /**
     * Метод проверки наличия уже созданного пользователя в хранилище по логину.
     *
     * @param loginToFind логин для поиска.
     * @param excludeId   идентификатор пользователя, исключаемый из результатов поиска.
     */
    private void isLoginUsed(String loginToFind, long excludeId) throws ValidationException {
        if (loginToFind != null && !loginToFind.isBlank()) {
            Collection<User> users = userStorage.findAll();
            Optional<User> userWithLogin = users
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
     * Метод проверки наличия указанного пользователя в хранилище.
     *
     * @param userId    проверяемый идентификатор.
     * @param paramName название проверяемой сущности.
     */
    void checkUserId(long userId, String paramName) {
        log.info("User checkUserId. " + paramName + " = " + userId);

        if (userId == 0) {
            log.error("Wrong " + paramName);
            throw new ValidationException(paramName + " должен быть указан.");
        }
        userStorage.findById(userId);
    }
}
