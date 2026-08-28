package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryUserStorage implements UserStorage {
    private Map<Long, User> users = new HashMap<>();

    @Override
    public User createUser(User newUser) {
        log.info("Попытка создать пользователя: {}", newUser.getName());
        for (ru.yandex.practicum.filmorate.model.User user : users.values()) {
            if (user.getName().equals(newUser.getName())) {
                log.warn("пользователь с именем {} уже существует.", newUser.getName());
                throw new DuplicatedDataException("Пользователь с таким именем уже существует");
            }
        }
        newUser.setId(getNextId());
        users.put(newUser.getId(), newUser);
        log.info("Пользователь создан: id = {}, name = {}", newUser.getId(), newUser.getName());
        return newUser;
    }

    @Override
    public User updateUser(User updatedUser) {
        Long currentIdUser = updatedUser.getId();
        log.info("Попытка обновления данных пользователя {}.", updatedUser.getName());
        if (users.get(currentIdUser) == null) {
            log.warn("Невозможно обновить информацию о пользователе {}! " +
                    "Т.к. данного пользователя не существует.", updatedUser.getName());
            throw new NotFoundException("Такого пользователя не существует");
        }
        users.put(currentIdUser, updatedUser);
        log.info("Данные о пользователе {} обновлены.", updatedUser.getName());
        return updatedUser;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> usersList = new ArrayList<>();
        for (User user : users.values()) {
            usersList.add(user);
        }
        log.info("Получение актуального списка всех пользователей.");
        return usersList;
    }

    @Override
    public User getUserById(Long id) {
        User user = users.get(id);
        if (user == null) {
            throw new NotFoundException("Фильм с id " + id + "не найден");
        }
        return user;
    }

    @Override
    public void deleteUser(long id) {
        users.remove(id);
    }

    public Long getNextId() {
        Long newId = users.keySet()
                .stream()
                .max(Long::compare)
                .orElse(0L);
        return newId + 1;
    }
}
