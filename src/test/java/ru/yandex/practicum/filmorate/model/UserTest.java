package ru.yandex.practicum.filmorate.model;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class UserTest {
    private UserController userController = new UserController();
    private User user;

    @BeforeEach
    void prepareUser() {
        user = new User();
        user.setEmail("10sprint@yandex.ru");
        user.setLogin("practindex");
        user.setName("MoviesWatcher");
        user.setBirthday(LocalDate.of(1995, 6, 1));
    }

    @Test
    public void shouldCreateUserWithValidData() {
        assertDoesNotThrow(() -> {
            userController.validateEmail(user.getEmail());
            userController.validateLogin(user.getLogin());
            userController.validateName(user);
            userController.validateBirthday(user.getBirthday());
        });
    }

    @Test
    public void shouldThrowExceptionWhenEmailIsNull() {
        user.setEmail(null);
        assertThrows(ValidationException.class, () -> {
            userController.validateEmail(user.getEmail());
        });
    }

    @Test
    public void shouldThrowExceptionWhenEmailHasNoAtSymbol() {
        user.setEmail("10sprintyandex.ru");
        assertThrows(ValidationException.class, () -> {
            userController.validateEmail(user.getEmail());
        });
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsNull() {
        user.setLogin(null);
        assertThrows(ValidationException.class,() -> {
            userController.validateLogin(user.getLogin());
        });
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsHasSpace() {
        user.setLogin("practi ndex");
        assertThrows(ValidationException.class,() -> {
            userController.validateLogin(user.getLogin());
        });
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsOnlySpaces() {
        user.setLogin("   ");
        assertThrows(ValidationException.class,() -> {
            userController.validateLogin(user.getLogin());
        });
    }

    @Test
    public void shouldReplaceNullNameWithLogin() {
        user.setName(null);
        userController.validateName(user);
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void shouldReplaceBlankNameWithLogin() {
        user.setName("");
        userController.validateName(user);
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void shouldReplaceSpacesNameWithLogin() {
        user.setName("   ");
        userController.validateName(user);
        assertEquals(user.getLogin(), user.getName());
    }

    @Test
    public void shouldThrowExceptionWhenBirthdayIsNull() {
        user.setBirthday(null);
        assertThrows(ValidationException.class,() -> {
            userController.validateBirthday(user.getBirthday());
        });
    }

    @Test
    public void shouldThrowExceptionWhenBirthdayIsFuture() {
        user.setBirthday(LocalDate.of(2500,9,15));
        assertThrows(ValidationException.class,() -> {
            userController.validateBirthday(user.getBirthday());
        });
    }
}