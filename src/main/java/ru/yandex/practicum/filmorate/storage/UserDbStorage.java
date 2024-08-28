package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsExtractor;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Реализация репозитория работы с пользователями с хранением в базе данных.
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Repository
@Primary
@Slf4j
public class UserDbStorage extends BaseRepository<User> implements UserStorage {
    private static final String INSERT_QUERY = "INSERT INTO users (email, login, name, birthday)" +
            "VALUES (:email, :login, :name, :birthday)";
    private static final String UPDATE_QUERY = "UPDATE users SET email = :email, login = :login, name = :name, " +
            "birthday = :birthday WHERE user_id = :userId";
    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE user_id = :userId";
    private static final String ADD_FRIEND = "MERGE INTO friends (user_id, friend_id) VALUES (:userId, :friendId)";
    private static final String DEL_FRIEND = "DELETE FROM friends WHERE user_id = :userId AND friend_id = :friendId";
    private static final String FIND_ALL_FRIENDS_ID_BY_SET = "SELECT user_id, friend_id FROM friends " +
            "WHERE user_id in (:userIdSet)";
    private static final String FIND_ALL_FRIENDS_ID = "SELECT user_id, friend_id FROM friends";
    private static final String FIND_ALL_FRIENDS_ID_BY_USERID = "SELECT friend_id FROM friends WHERE user_id = :userId";
    private static final String FIND_ALL_FRIENDS_BY_USERID = "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
            "FROM friends f " +
            "INNER JOIN users u ON f.friend_id = u.user_id " +
            "WHERE f.user_id = :userId";
    private static final String FIND_COMMON_FRIENDS = "SELECT u.user_id, u.email, u.login, u.name, u.birthday " +
            "FROM friends f " +
            "INNER JOIN users u ON f.friend_id = u.user_id " +
            "INNER JOIN friends f2 ON f.friend_id = f2.friend_id " +
            "WHERE f.user_id = :userId " +
            "AND f2.user_id = :otherId";

    @Autowired
    private UserFriendsExtractor friendsExtractor;

    public UserDbStorage(NamedParameterJdbcOperations jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    /**
     * Метод получения всех пользователей сервиса.
     *
     * @return Коллекция пользователей.
     */
    @Override
    public Collection<User> findAll() {
        log.info("User findAll entering.");
        Collection<User> users = findMany(FIND_ALL_QUERY);
        Map<Long, Set<Long>> usersFriends = jdbc.query(FIND_ALL_FRIENDS_ID, friendsExtractor);
        users = users.stream()
                .map(user -> {
                    user.setFriendsUserId(usersFriends.get(user.getId()));
                    return user;
                })
                .collect(Collectors.toList());

        return users;
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
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        Optional<User> foundUser = findOne(FIND_BY_ID_QUERY, params);
        if (foundUser.isEmpty()) {
            throw new NotFoundException("Пользователь с id = " + userId + " не найден.");
        }

        User user = foundUser.get();
        List<User> allFriends = getAllFriends(userId);
        Set<Long> friendsUserId = allFriends.stream().map(User::getId).collect(Collectors.toSet());
        user.setFriendsUserId(friendsUserId);

        return foundUser.get();
    }

    /**
     * Метод добавления пользователя.
     *
     * @param newUser добавляемый пользователь.
     * @return Добавленный пользователь.
     */
    @Override
    public User create(User newUser) {
        log.info("User create. newUser = " + newUser);
        fillEmptyName(newUser, "create");
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", newUser.getEmail());
        params.addValue("login", newUser.getLogin());
        params.addValue("name", newUser.getName());
        params.addValue("birthday", newUser.getBirthday());

        long id = insert(INSERT_QUERY, params);
        newUser.setId(id);

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
        log.info("User update. user = " + user);
        fillEmptyName(user, "update");
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("email", user.getEmail());
        params.addValue("login", user.getLogin());
        params.addValue("name", user.getName());
        params.addValue("birthday", user.getBirthday());
        params.addValue("userId", user.getId());

        update(UPDATE_QUERY, params);

        return user;
    }

    /**
     * Метод добавления в друзья.
     *
     * @param userId      пользователь, которому добавляется друг.
     * @param friendId    пользователь, добавляемый в друзья.
     * @param isRecursive признак рекурсивного вызова для исключения зацикливания.
     * @return Список id друзей пользователя userId.
     */
    @Override
    public List<Long> addFriend(long userId, long friendId, boolean isRecursive) {
        log.info("User addFriend. userId = " + userId + ", friendId = " + friendId + ", isRecursive = " + isRecursive);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("friendId", friendId);

        jdbc.update(ADD_FRIEND, params);

        return jdbc.queryForList(FIND_ALL_FRIENDS_ID_BY_USERID, params, Long.class);
    }

    /**
     * Метод удаления из друзей
     *
     * @param userId      пользователь, который лишается друга.
     * @param friendId    пользователь, убираемый из друзей.
     * @param isRecursive признак рекурсивного вызова для исключения зацикливания.
     * @return Список id друзей пользователя userId.
     */
    @Override
    public List<Long> deleteFriend(long userId, long friendId, boolean isRecursive) {
        log.info("User deleteFriend. userId = " + userId + ", friendId = " + friendId + ", isRecursive = "
                + isRecursive);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("friendId", friendId);

        jdbc.update(DEL_FRIEND, params);

        return jdbc.queryForList(FIND_ALL_FRIENDS_ID_BY_USERID, params, Long.class);
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
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);

        List<User> friends = jdbc.query(FIND_ALL_FRIENDS_BY_USERID, params, mapper);
        friends = getFriendsOfFriends(friends);

        return friends;
    }

    /**
     * Метод получения друзей пользователя, общих с другим пользователем.
     *
     * @param userId  пользователь, по которому необходимо вывести друзей.
     * @param otherId другой пользователь, для поиска общих друзей с ним.
     * @return Список общих друзей.
     */
    @Override
    public List<User> getCommonFriends(long userId, long otherId) {
        log.info("User getCommonFriends. userId = " + userId + ", otherId = " + otherId);
        MapSqlParameterSource params = new MapSqlParameterSource();
        params.addValue("userId", userId);
        params.addValue("otherId", otherId);

        List<User> friends = jdbc.query(FIND_COMMON_FRIENDS, params, mapper);
        friends = getFriendsOfFriends(friends);

        return friends;
    }

    /**
     * Метод заполнения множества id друзей {@link User#setFriendsUserId(Set)} у заданного списка.
     * Обычно это друзья друзей.
     *
     * @param friends Список пользователей-друзей
     * @return Список пользователей-друзей
     */
    private List<User> getFriendsOfFriends(List<User> friends) {
        log.info("User getCommonFriends. friends = " + friends);
        MapSqlParameterSource params = new MapSqlParameterSource();
        Set<Long> friendsId = friends.stream()
                .map(User::getId)
                .collect(Collectors.toSet());
        params.addValue("userIdSet", friendsId);
        Map<Long, Set<Long>> usersFriends = jdbc.query(FIND_ALL_FRIENDS_ID_BY_SET, params, friendsExtractor);

        friends = friends.stream()
                .map(friend -> {
                    friend.setFriendsUserId(usersFriends.get(friend.getId()));
                    return friend;
                })
                .collect(Collectors.toList());

        return friends;
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
