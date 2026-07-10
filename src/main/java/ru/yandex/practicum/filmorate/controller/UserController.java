package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@Slf4j
@RequestMapping("/users")
public class UserController {
    private Map<Long, User> users = new HashMap<>();

    @PostMapping
    public User createUser(@Valid @RequestBody User newUser) {
        log.info("Попытка создать пользователя: {}", newUser.getName());
        validateEmail(newUser.getEmail());
        validateLogin(newUser.getLogin());
        validateName(newUser);
        validateBirthday(newUser.getBirthday());

        for (User user : users.values()) {
            if (user.getName().equals(newUser.getName())) {
                log.warn("Невозможно создать пользователя с таким именем. Такой пользователь уже существует.");
                throw new DuplicatedDataException("Пользователь с таким именем уже существует");
            }
        }
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь создан: id = {}, name = {}", newUser.getId(), newUser.getName());
        return newUser;
    }

    @PutMapping
    public User updateUser(@RequestBody User updatedUser) {
        Long currentIdUser = updatedUser.getId();
            log.info("Попытка обновления данных пользователя {}.", updatedUser.getName());
        if (users.get(currentIdUser) == null) {
            log.warn("Невозможно обновить информацию о пользователе {}! " +
                    "Т.к. данного пользователя не существует.", updatedUser.getName());
            throw new NotFoundException("Такого пользователя не существует");
        }

        validateEmail(updatedUser.getEmail());
        validateLogin(updatedUser.getLogin());
        validateName(updatedUser);
        validateBirthday(updatedUser.getBirthday());

        users.put(currentIdUser, updatedUser);
        log.info("Данные о пользователе {} обновлены.", updatedUser.getName());
        return updatedUser;
    }

    @GetMapping
    public List<User> getAllUsers() {
        List<User> usersList = new ArrayList<>();
        for (User user : users.values()) {
            usersList.add(user);
        }
        log.info("Получение актуального списка всех пользователей.");
        return usersList;
    }

    public Long getNextId() {
        Long newId = users.keySet()
                .stream()
                .max(Long::compare)
                .orElse(0L);
        return newId + 1;
    }

    //    Проверка Email на отсутствие символов и на наличии собачки
    public void validateEmail(String email) {
        log.info("Проверка эл.почты(Email) на отсутствие символов и на наличие символа '@'.");
        if (email == null) {
            log.warn("Эл.почта(Email) не введена.");
            throw new ValidationException("Email не может быть пустым");
        }
        if (!email.contains("@")) {
            log.warn("В указанной эл.почте(Email) отсутствует символ '@'.");
            throw new ValidationException("Нельзя просто так взять и не поставить '@'");
        }
        log.info("Эл.почта(Email) указана корректно: {}.", email);
    }

    // Проверка логина на пустоту и на пробелы
    public void validateLogin(String login) {
        log.info("Проверка login на отсутствие символов и на наличие пробелов.");
        if (login == null) {
            log.warn("Не был введён login.");
            throw new ValidationException("Логин не может быть пустым");
        }
        if (login.contains(" ")) {
            log.warn("login содержит пробелы.");
            throw new ValidationException("Логин не должен содержать пробелы");
        }
        log.info("Логин указан корректно: {}.", login);
    }

    // Проверка на пустоту поля имени и заменой на login
    public void validateName(User user) {
        log.info("Проверка имени на отсутствие символов и его заменой на login.");
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Замена имени на login.");
            user.setName(user.getLogin());
        }
        log.info("Имя заменено на логин указан корректно: {}.", user.getLogin());
    }

    // Проверка на указание днюхи
    public void validateBirthday(LocalDate birthday) {
        log.info("Проверка на корректность указания даты рождения. Не позже текущей даты {}.", LocalDate.now());
        if (birthday == null) {
            log.warn("Не была указана дата рождения.");
            throw new ValidationException("Дата рождения должна быть указана");
        }
        if (birthday.isAfter(LocalDate.now())) {
            log.warn("Введена дата позже текущей даты. Введено: {}. Текущая дата: {}.", birthday, LocalDate.now());
            throw new ValidationException("День рождения не может быть в будущем");
        }
        log.info("Введена корректная дата рождения: {}.", birthday);
    }
}