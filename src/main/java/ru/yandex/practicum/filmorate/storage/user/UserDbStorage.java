package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mapper.UserRowMapper;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Slf4j
@Repository
@Qualifier("UserDbStorage")
public class UserDbStorage implements UserStorage {
    private JdbcTemplate jdbcTemplate;

    public UserDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public User createUser(User user) {
        log.info("Создание пользователя: {}", user.getLogin());
        String query = "INSERT INTO users(email, login, name, birthday) VALUES(?, ?, ?, ?);";
        Object[] params = {user.getEmail(), user.getLogin(), user.getName(), user.getBirthday()};

        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Long id = keyHolder.getKey().longValue();

        if (id != null) {
            user.setId(id);
            log.info("Пользователь {} создан с ID {}", user.getLogin(), id);
            return user;
        } else {
            log.warn("Не удалось создать пользователя с ID: {}", user.getLogin());
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    @Override
    public User getUserById (Long id) {
        log.debug("Запрос пользователя по ID: {}", id);
        String query = "SELECT * FROM users WHERE id = ?;";
        try {
            User user = jdbcTemplate.queryForObject(query, new UserRowMapper(), id);
            log.info("Пользователь с ID {} найден", id);
            return user;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с ID {} не найден", id);
            return null;
        }
    }

    @Override
    public List<User> getAllUsers () {
        log.info("Запрос списка всех пользователей");
        String query ="SELECT * FROM users;";
        List<User> listUsers = jdbcTemplate.query(query, new UserRowMapper());
        log.debug("Список сформирован. Найдено {} пользователей", listUsers.size());
        return listUsers;
    }

    @Override
    public User updateUser(User user) {
        log.info("Обновление пользователя с ID: {}", user.getId());
        User existingUser = getUserById(user.getId());
        if (existingUser == null) {
            log.warn("Пользователь с ID {} не найден для обновления", user.getId());
            throw new NotFoundException("Пользователь с ID " + user.getId() + " не найден");
        }

        String query = "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
        int rowsUpdated = jdbcTemplate.update(query,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Не удалось обновить пользователя с ID {}", user.getId());
            throw new InternalServerException("Не удалось обновить данные");
        }
        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return user;
    }

    @Override
    public void deleteUser(long id) {
        log.info("Запрос на удаление пользователя c ID {}", id);
        String query ="DELETE FROM users WHERE id = ?;";
        int rowsDeleted = jdbcTemplate.update(query, id);
        if(rowsDeleted == 0) {
            log.warn("Нельзя удалить пользователя если его нет в базе");
            throw new NotFoundException("Пользователь с Id " + id + "не найден!");
        }
        log.info("Пользователь удалён c ID {}", id);
    }

    public void addFriend(Long userId, Long friendId) {
        log.info("Попытка добавить в друзья пользователя с ID {} пользователем c ID {}", friendId, userId);
        if (getUserById(userId) == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (getUserById(friendId) == null) {
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }
        if (userId.equals(friendId)) {
            throw new DuplicatedDataException("Нельзя добавить себя в друзья");
        }
        String query = "INSERT INTO friendship (user_id, friend_id) VALUES (?, ?)";
        try {
            jdbcTemplate.update(query, userId, friendId);
            log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
        } catch (DataAccessException e) {
            log.warn("Пользователи уже друганы!!!");
            throw new DuplicatedDataException("Пользователи уже дружат");
        }
    }

    public void deleteFriend(Long userId, Long friendId) {
        log.info("Попытка пользователя с ID {} удалить пользователя с ID {} из друзей", userId, friendId);
        User user = getUserById(userId);
        if (user == null) {
            log.warn("Пользователь с ID {} не найден для удаления друга", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        User friend = getUserById(friendId);
        if (friend == null) {
            log.warn("Пользователь с ID {} не найден для удаления из друзей", friendId);
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            log.warn("Пользователь с ID {} попытался удалить себя из друзей", userId);
            throw new DuplicatedDataException("Нельзя удалить себя");
        }

        String query = "DELETE FROM friendship WHERE user_id = ? AND friend_id = ?";
        int rowsDeleted = jdbcTemplate.update(query, userId, friendId);
        if (rowsDeleted == 0) {
            log.info("Пользователи не находятся друг у друга в друзьях");
            return;
        }
        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
    }

    public List<User> getFriends(Long userId) {
        log.info("Запрос на получение списка друзей пользователя c ID {}", userId);
        User user = getUserById(userId);
        if (user == null) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        String query = "SELECT * FROM users " +
                "INNER JOIN friendship ON users.id = friendship.friend_id " +
                "WHERE friendship.user_id = ?";
        List<User> listFriends = jdbcTemplate.query(query, new UserRowMapper(), userId);
        log.info("Получение списка друзей пользователя с ID {}", userId);
        return listFriends;
    }

    public List<User> getMutualFriends(Long userId, Long otherUserId) {
        log.info("Запрос на получение общих друзей пользователей c ID {} и c ID {}", userId, otherUserId);
        String query = "SELECT * FROM users AS us " +
                "INNER JOIN friendship AS fr1 ON us.id = fr1.friend_id " +
                "INNER JOIN friendship AS fr2 ON us.id = fr2.friend_id " +
                "WHERE fr1.user_id = ? AND fr2.user_id = ?";
        List<User> listFriends = jdbcTemplate.query(query, new UserRowMapper(), userId, otherUserId);
        log.info("Получение списка общих друзей пользователей c ID {} и c ID {}", userId, otherUserId);
        return listFriends;
    }
}
