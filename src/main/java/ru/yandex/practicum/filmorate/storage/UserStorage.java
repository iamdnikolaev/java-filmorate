package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.List;

public interface UserStorage {
    Collection<User> findAll();

    User findById(long userId);

    User create(User newUser);

    User update(User user);

    List<Long> addFriend(long userId, long friendId, boolean isRecursive);

    List<Long> deleteFriend(long userId, long friendId, boolean isRecursive);

    List<User> getAllFriends(long userId);

    List<User> getCommonFriends(long userId, long otherId);
}
