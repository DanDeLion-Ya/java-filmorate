package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mpa.MpaDbStorage;

import java.util.List;

@Slf4j
@Service
public class MpaService {
    private MpaDbStorage mpaDbStorage;

    public MpaService(MpaDbStorage mpaDbStorage) {
        this.mpaDbStorage = mpaDbStorage;
    }

    public Mpa getMpaById(long id) {
        log.info("Запрос MPA по ID: {}", id);
        Mpa mpa = mpaDbStorage.getMpaById(id);
        if (mpa == null) {
            log.warn("MPA с ID {} не найден", id);
            throw new NotFoundException("MPA с ID " + id + " не найден");
        }
        return mpa;
    }

    public List<Mpa> getAllMpa() {
        return mpaDbStorage.getAllMpa();
    }
}
