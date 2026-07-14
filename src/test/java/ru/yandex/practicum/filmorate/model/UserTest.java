package ru.yandex.practicum.filmorate.model;

import static org.junit.jupiter.api.Assertions.*;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;

import java.time.LocalDate;
import java.util.Set;

public class UserTest {
    private UserController userController = new UserController();
    private User user;
    private Validator validator;

    @BeforeEach
    void prepareUser() {
        user = new User();
        user.setEmail("10sprint@yandex.ru");
        user.setLogin("practindex");
        user.setName("MoviesWatcher");
        user.setBirthday(LocalDate.of(1995, 6, 1));

        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @Test
    public void shouldCreateUserWithValidData() {
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertTrue(violations.isEmpty());
    }

    @Test
    public void shouldThrowExceptionWhenEmailIsNull() {
        user.setEmail(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("Email не может быть пустым", textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenEmailHasNoAtSymbol() {
        user.setEmail("10sprintyandex.ru");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("Нельзя просто так взять и не поставить '@'", textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsNull() {
        user.setLogin(null);
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("Логин не может быть пустым и содержать пробелы", textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsHasSpace() {
        user.setLogin("practi ndex");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("Логин не может быть пустым и содержать пробелы", textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenLoginIsOnlySpaces() {
        user.setLogin("   ");
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(2, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("Логин не может быть пустым и содержать пробелы", textViolation.getMessage());
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
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("Дата рождения должна быть указана", textViolation.getMessage());
    }

    @Test
    public void shouldThrowExceptionWhenBirthdayIsFuture() {
        user.setBirthday(LocalDate.of(2500,9,15));
        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertEquals(1, violations.size());

        ConstraintViolation<User> textViolation = violations.iterator().next();
        assertEquals("День рождения не может быть в будущем", textViolation.getMessage());
    }
}