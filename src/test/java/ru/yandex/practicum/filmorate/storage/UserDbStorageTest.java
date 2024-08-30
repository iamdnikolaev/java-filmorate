package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserFriendsExtractor;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@JdbcTest
@Import({UserDbStorage.class, UserRowMapper.class, UserFriendsExtractor.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@DisplayName("UserDbStorage")
class UserDbStorageTest {
    private static final long TEST_USER_ID = 1L;
    private static final long TEST_NEWUSER_ID = 4L;
    private final UserDbStorage userDbStorage;

    static User getTestUser() {
        User user = User.builder()
                .id(TEST_USER_ID)
                .email("email1@email.com")
                .login("userlogin1")
                .name("testname1")
                .birthday(LocalDate.of(2001, 1, 11))
                .build();

        return user;
    }

    static List<User> getTestUserList() {
        List<User> resultList = new ArrayList<>();
        for (int i = 1; i < 4; i++) {
            User user = User.builder()
                    .id((long) i)
                    .email("email" + i + "@email.com")
                    .login("userlogin" + i)
                    .name("testname" + i)
                    .birthday(LocalDate.of(2001, 1, 10).plusDays(i))
                    .build();

            resultList.add(user);
        }
        return resultList;
    }

    @Test
    @DisplayName("Должен найти всех пользователей")
    void should_return_all_users() {
        List<User> testUserList = getTestUserList();

        Optional<Collection<User>> usersOptional = Optional.ofNullable(userDbStorage.findAll());
        assertTrue(usersOptional.isPresent(), "Пользователи получены.");
        assertEquals(testUserList.size(), usersOptional.get().size(), "Неверное количество пользователей.");
        List<User> foundUserList = usersOptional.get().stream().toList();

        for (int i = 0; i < testUserList.size(); i++) {
            assertThat(foundUserList.get(i))
                    .usingRecursiveComparison()
                    .ignoringExpectedNullFields()
                    .isEqualTo(testUserList.get(i));
        }
    }

    @Test
    @DisplayName("Должен найти пользователя по id = 1")
    void should_return_user_when_find_by_id() {
        User user = userDbStorage.findById(TEST_USER_ID);

        assertThat(user)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(getTestUser());
    }

    @Test
    @DisplayName("Должен создать пользователя 4")
    void should_create_new_user() {
        User newUser = User.builder()
                .id(TEST_NEWUSER_ID)
                .email("email4@email.com")
                .login("userlogin4")
                .name("testname4")
                .birthday(LocalDate.of(2001, 1, 14))
                .build();
        newUser = userDbStorage.create(newUser);

        User madeUser = userDbStorage.findById(newUser.getId());

        assertThat(madeUser)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(newUser);
    }

    @Test
    @DisplayName("Должен изменить атрибуты имеющегося пользователя 1")
    void should_update_user() {
        User oldUser = userDbStorage.findById(1);
        oldUser.setEmail(oldUser.getEmail() + "changed");
        oldUser.setLogin(oldUser.getLogin() + "changed");
        oldUser.setName(oldUser.getName() + "changed");
        oldUser.setBirthday(oldUser.getBirthday().plusDays(1));

        User changedUser = userDbStorage.update(oldUser);
        changedUser = userDbStorage.findById(oldUser.getId());

        assertThat(changedUser)
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .isEqualTo(oldUser);
    }

    @Test
    @DisplayName("Должен добавить друга 2 пользователю 1")
    void should_add_one_friend_to_user() {
        userDbStorage.addFriend(1, 2, false);
        List<User> friends = userDbStorage.getAllFriends(1);
        User friend = userDbStorage.findById(2);

        assertThat(friends.getFirst())
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringActualNullFields()
                .isEqualTo(friend);
    }

    @Test
    @DisplayName("Должен убрать друга у пользователя 3")
    void should_delete_friend_of_user() {
        List<User> friends = userDbStorage.getAllFriends(3);
        assertEquals(1, friends.size(), "Неверное исходное количество друзей.");

        List<Long> friendsAfterDelete = userDbStorage.deleteFriend(3, 2, false);
        assertEquals(0, friendsAfterDelete.size(), "Неверное количество друзей после удаления.");
    }

    @Test
    @DisplayName("Должен выдать всех друзей пользователя 3")
    void getAllFriends() {
        List<User> friends = userDbStorage.getAllFriends(3);
        assertEquals(1, friends.size(), "Неверное количество друзей.");
    }

    @Test
    @DisplayName("Должен выдать общих друзей (пользователь 2) пользователей 1 и 3")
    void getCommonFriends() {
        userDbStorage.addFriend(1, 2, false);

        List<User> commonFriends = userDbStorage.getCommonFriends(1, 3);
        assertEquals(1, commonFriends.size(), "Неверное количество общих друзей.");

        User friend = userDbStorage.findById(2);

        assertThat(commonFriends.getFirst())
                .usingRecursiveComparison()
                .ignoringExpectedNullFields()
                .ignoringActualNullFields()
                .isEqualTo(friend);
    }
}