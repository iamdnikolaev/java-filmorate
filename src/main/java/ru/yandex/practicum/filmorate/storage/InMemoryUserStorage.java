package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Реализация интерфейса работы с пользователями с хранением в памяти.
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    /**
     * Хранилище пользователей.
     */
    private final Map<Long, User> users = new HashMap<>();

    /**
     * Метод получения всех пользователей сервиса.
     *
     * @return Коллекция пользователей.
     */
    @Override
    public Collection<User> findAll() {
        log.info("findAll. users = " + users);
        return users.values();
    }

    /**
     * Метод получения данных по конкретному пользователю.
     *
     * @param userId пользователь для поиска.
     * @return Данные пользователя.
     */
    @Override
    public User findById(long userId) {
        log.info("User findById. userId = " + userId);
        return users.values().stream()
                .filter(x -> x.getId() == userId)
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не найден."));
    }

    /**
     * Метод добавления пользователя.
     *
     * @param newUser добавляемый пользователь.
     * @return Добавленный пользователь.
     */
    @Override
    public User create(User newUser) {
        fillEmptyName(newUser, "create");
        newUser.setId(getNextId());

        log.info("User create. newUser = " + newUser);
        users.put(newUser.getId(), newUser);
        return newUser;
    }

    /**
     * Метод изменения пользователя.
     *
     * @param user пользователь с новыми атрибутами.
     * @return Пользователь после изменения.
     */
    @Override
    public User update(User user) {
        fillEmptyName(user, "update");

        User oldUser = users.get(user.getId());
        log.info("User update. user = " + user);
        oldUser.setLogin(user.getLogin());
        oldUser.setEmail(user.getEmail());
        oldUser.setName(user.getName());
        oldUser.setBirthday(user.getBirthday());

        return oldUser;
    }

    /**
     * Метод добавления в друзья.
     *
     * @param userId   пользователь, которому добавляется друг.
     * @param friendId пользователь, добавляемый в друзья.
     * @return Список друзей пользователя userId.
     */
    @Override
    public List<Long> addFriend(long userId, long friendId, boolean isRecursive) {
        log.info("User addFriend. userId = " + userId + ", friendId = " + friendId + ", isRecursive = " + isRecursive);
        User user = users.get(userId);
        Set<Long> friends = user.getFriendsUserId();
        if (friends == null) {
            friends = new HashSet<>();
            user.setFriendsUserId(friends);
        }
        friends.add(friendId);

        if (!isRecursive) {
            addFriend(friendId, userId, true);
        }

        return friends.stream().toList();
    }

    /**
     * Метод удаления из друзей
     *
     * @param userId   пользователь, который лишается друга.
     * @param friendId пользователь, убираемый из друзей.
     * @return Список друзей пользователя userId.
     */
    @Override
    public List<Long> deleteFriend(long userId, long friendId, boolean isRecursive) {
        log.info("User deleteFriend. userId = " + userId + ", friendId = " + friendId + ", isRecursive = "
                + isRecursive);
        User user = users.get(userId);
        Set<Long> friends = user.getFriendsUserId();
        if (friends != null && friends.contains(friendId)) {
            friends.remove(friendId);
        }

        if (!isRecursive) {
            deleteFriend(friendId, userId, true);
        }

        List<Long> result = null;
        if (friends != null) {
            result = friends.stream().toList();
        }
        return result;
    }

    /**
     * Метод получения всех друзей пользователя.
     *
     * @param userId пользователь, по которому необходимо вывести друзей.
     * @return Список друзей пользователя.
     */
    @Override
    public List<User> getAllFriends(long userId) {
        log.info("User getAllFriends. userId = " + userId);
        User user = users.get(userId);
        Set<Long> friends = user.getFriendsUserId();
        List<User> result = new ArrayList<>();
        if (friends != null) {
            result = friends.stream()
                    .map(users::get)
                    .toList();
        }

        return result;
    }

    /**
     * Метод получения друзей пользователя, общих с другим пользователем.
     *
     * @param userId  пользователь, по которому необходимо вывести друзей.
     * @param otherId другой пользователь, для поиска общих друзей с ним.
     * @return Список id общих друзей.
     */
    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        log.info("User getCommonFriends. userId = " + userId + ", otherId = " + otherId);
        User user = users.get(userId);
        User other = users.get(otherId);
        Set<Long> userFriends = user.getFriendsUserId();
        Set<Long> otherFriends = other.getFriendsUserId();

        List<User> result = new ArrayList<>();
        if (userFriends != null && otherFriends != null) {
            result = userFriends.stream()
                    .filter(otherFriends::contains)
                    .map(users::get)
                    .toList();
        }

        return result;
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

    /**
     * Метод заполнения пустого имени пользователя логином.
     *
     * @param user       проверяемый пользователь
     * @param actionName имя операции для записи в лог
     */
    private void fillEmptyName(User user, String actionName) {
        if (user.getName() == null || user.getName().isBlank()) {
            log.debug("fillEmptyName. User " + actionName + " . Filling empty name by login.");
            user.setName(user.getLogin());
        }
    }
}
