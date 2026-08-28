package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {
    private UserStorage userStorage;
    private UserDbStorage userDbStorage;

    @Autowired
    public UserService(@Qualifier("UserDbStorage") UserStorage userStorage,
                       UserDbStorage userDbStorage) {
        this.userStorage = userStorage;
        this.userDbStorage = userDbStorage;
    }

    public User createUser(User newUser) {
        validateName(newUser);
        return userStorage.createUser(newUser);
    }

    public User updateUser(User updatedUser) {
        validateName(updatedUser);
        return userStorage.updateUser(updatedUser);
    }

    public void deleteUser(long id) {
        userStorage.deleteUser(id);
    }

    public List<User> getAllUsers() {
        return userStorage.getAllUsers();
    }

    public User getUserById(Long id) {
        if (id == null) {
            log.warn("ID пользователя не передан!");
            throw new NotFoundException("ID пользователя не может быть null!");
        }
        log.info("Запрос пользователя по ID: {}", id);
        return userStorage.getUserById(id);
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

    public void addFriends(Long userId, Long friendId) {
        userDbStorage.addFriend(userId, friendId);
    }

    public void deleteFriend(Long userId, Long friendId) {
        userDbStorage.deleteFriend(userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        return userDbStorage.getFriends(userId);
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
        return userDbStorage.getMutualFriends(userId, friendId);
    }
}