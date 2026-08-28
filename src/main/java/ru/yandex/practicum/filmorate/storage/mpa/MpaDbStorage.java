package ru.yandex.practicum.filmorate.storage.mpa;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MpaRowMapper;

import java.util.List;

@Repository
public class MpaDbStorage implements MpaStorage {
    private final JdbcTemplate jdbcTemplate;

    public MpaDbStorage(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Mpa getMpaById(Long id) {
        String query = "SELECT * FROM mpa WHERE id = ?;";
        try {
            return jdbcTemplate.queryForObject(query, new MpaRowMapper(), id);
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    @Override
    public List<Mpa> getAllMpa() {
        String query = "SELECT * FROM mpa;";
        try {
            return jdbcTemplate.query(query, new MpaRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return List.of();
        }
    }
}
