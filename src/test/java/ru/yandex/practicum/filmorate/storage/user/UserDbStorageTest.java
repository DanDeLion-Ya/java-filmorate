package ru.yandex.practicum.filmorate.storage.user;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(UserDbStorage.class)
public class UserDbStorageTest {
    private final UserDbStorage userStorage;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("test@yandex.ru.");
        user.setLogin("testogin");
        user.setName("JohnTestowick");
        user.setBirthday(LocalDate.of(2014,10,24));
    }

    @Test
    public void testCreateUser() {
        User createdUser = userStorage.createUser(user);
        assertThat(createdUser).isNotNull();
        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getEmail()).isEqualTo(user.getEmail());
        assertThat(createdUser.getLogin()).isEqualTo(user.getLogin());
        assertThat(createdUser.getName()).isEqualTo(user.getName());
        assertThat(createdUser.getBirthday()).isEqualTo(user.getBirthday());
    }

    @Test
    public void testGetUserById() {
        User createdUser = userStorage.createUser(user);
        User findUser = userStorage.getUserById(createdUser.getId());
        assertThat(findUser).isNotNull();
        assertThat(findUser.getId()).isEqualTo(createdUser.getId());
    }

    @Test
    public void testUpdateUser() {
        User createdUser = userStorage.createUser(user);
        createdUser.setLogin("FilmoTester");
        createdUser.setName("UpdaTminator");

        User updatedUser = userStorage.updateUser(createdUser);
        assertThat(updatedUser).isNotNull();
        assertThat(updatedUser.getId()).isEqualTo(createdUser.getId());
        assertThat(updatedUser.getEmail()).isEqualTo(createdUser.getEmail());
        assertThat(updatedUser.getLogin()).isEqualTo("FilmoTester");
        assertThat(updatedUser.getName()).isEqualTo("UpdaTminator");
        assertThat(updatedUser.getBirthday()).isEqualTo(createdUser.getBirthday());
    }

    @Test
    public void testDeleteUser() {
        User createdUser = userStorage.createUser(user);
        userStorage.deleteUser(createdUser.getId());
        User userFromDb = userStorage.getUserById(createdUser.getId());
        assertThat(userFromDb).isNull();
    }

    @Test
    public void testGetAllUsers() {
        User user1 = userStorage.createUser(user);

        User user2 = new User();
        user2.setEmail("proverka@yandex.ru.");
        user2.setLogin("proverogin");
        user2.setName("Provernator");
        user2.setBirthday(LocalDate.of(1984,10,28));
        userStorage.createUser(user2);

        User user3 = new User();
        user3.setEmail("control@yandex.ru.");
        user3.setLogin("conrogin");
        user3.setName("Contrum");
        user3.setBirthday(LocalDate.of(1975,6,18));
        userStorage.createUser(user3);

        List<User> allUsers = userStorage.getAllUsers();
        assertThat(allUsers.size()).isEqualTo(3);
    }
}
