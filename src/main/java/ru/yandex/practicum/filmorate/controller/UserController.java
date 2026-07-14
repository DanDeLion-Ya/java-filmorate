package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

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
        validateName(newUser);
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
    public User updateUser(@Valid @RequestBody User updatedUser) {
        Long currentIdUser = updatedUser.getId();
            log.info("Попытка обновления данных пользователя {}.", updatedUser.getName());
        if (users.get(currentIdUser) == null) {
            log.warn("Невозможно обновить информацию о пользователе {}! " +
                    "Т.к. данного пользователя не существует.", updatedUser.getName());
            throw new NotFoundException("Такого пользователя не существует");
        }
        validateName(updatedUser);
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

    // Проверка на пустоту поля имени и заменой на login
    public void validateName(User user) {
        log.info("Проверка имени на отсутствие символов и его заменой на login.");
        if (user.getName() == null || user.getName().isBlank()) {
            log.info("Замена пустого имени на login");
            user.setName(user.getLogin());
        }
        log.info("Имя заменено на логин: {}.", user.getLogin());
    }
}