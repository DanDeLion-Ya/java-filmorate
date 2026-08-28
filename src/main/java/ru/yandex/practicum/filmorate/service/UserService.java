package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserDbStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.*;
import java.util.stream.Collectors;

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
//        log.info("Попытка пользователя с ID {} добавить в друзья пользователя с ID {}", userId, friendId);
//        User user = userStorage.getUserById(userId);
//        if (userId.equals(friendId)) {
//            log.warn("Пользователь не может добавить в друзья себя же!");
//            throw new DuplicatedDataException("Пользователь не может добавить сам себя в друзья!");
//        }
//        if (user.getFriends().contains(friendId)) {
//            log.warn("Пользователь с ID {} уже дружит с пользователем с ID {}!", userId, friendId);
//            throw new DuplicatedDataException("Данный пользователь и так уже есть у вас в друзьях!");
    }

    public void deleteFriend(Long userId, Long friendId) {
        userDbStorage.deleteFriend(userId, friendId);
//        log.info("Попытка пользователя с ID {} удалить из друзей пользователя с ID {}", userId, friendId);
//        User user = userStorage.getUserById(userId);
//        if (userId.equals(friendId)) {
//            log.warn("Пользователь не может удалить себя же!");
//            throw new DuplicatedDataException("Пользователь не может удалить самого себя!");
//        }
//
//        user.getFriends().remove(friendId);
//        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
//        log.info("Запрос списка друзей пользователя с ID {}", userId);
//        User user = userStorage.getUserById(userId);
//        List<Long> listFriendsOfUser = new ArrayList<>(user.getFriends());
//        List<User> result = listFriendsOfUser.stream()
//                .map(id -> userStorage.getUserById(id))
//                .collect(Collectors.toList());
//
//        log.info("У пользователя с ID {} было найдено {} друзей", userId, result.size());
        return userDbStorage.getFriends(userId);
    }

    public List<User> getMutualFriends(Long userId, Long friendId) {
//        log.info("Запрос списка общих друзей пользователя с ID {} и пользователя с ID {}", userId, friendId);
//        User user = userStorage.getUserById(userId);
//        User friend = userStorage.getUserById(friendId);
//        List<Long> listFriendsOfUser = new ArrayList<>(user.getFriends());
//        List<Long> listFriendsOfFriend = new ArrayList<>(friend.getFriends());
//        List<User> result = listFriendsOfUser.stream()
//                .filter(id -> listFriendsOfFriend.contains(id))
//                .map(id -> userStorage.getUserById(id))
//                .collect(Collectors.toList());
//
//        log.info("Найдено {} общих друзей у пользователей c ID {} и c ID {}.", result.size(), userId, friendId);
        return userDbStorage.getMutualFriends(userId, friendId);
    }
}