package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.DuplicatedDataException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {
    private Map<Long, Film> films = new HashMap<>();

    @Override
    public Film createFilm(Film newFilm) {
        log.info("Попытка добавить фильм в каталог name = {}", newFilm.getName());
        for (Film film : films.values()) {
            if (film.getName().equals(newFilm.getName())) {
                log.warn("Этот фильм {} уже есть в каталоге.", newFilm.getName());
                throw new DuplicatedDataException("Такой фильм уже есть");
            }
        }

        newFilm.setId(getNextId());
        films.put(newFilm.getId(), newFilm);
        log.info("Фильм добавлен в каталог: id = {}, name = {}.", newFilm.getId(), newFilm.getName());
        return newFilm;
    }

    @Override
    public Film updateFilm(Film updatedFilm) {
        Long currentIdFilm = updatedFilm.getId();
        log.info("Попытка обновления данных фильма {}.", updatedFilm.getName());
        if (films.get(currentIdFilm) == null) {
            log.warn("Невозможно обновить информацию о фильме {}! " +
                    "Такого фильма нет в каталоге.", updatedFilm.getName());
            throw new NotFoundException("Такого фильма нет списке");
        }
        films.put(currentIdFilm, updatedFilm);
        log.info("Данные о фильме {} обновлены.", updatedFilm.getName());
        return updatedFilm;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> filmsList = new ArrayList<>();
        for (Film film : films.values()) {
            filmsList.add(film);
        }
        log.info("Получение актуального списка фильмов в каталоге.");
        return filmsList;
    }

    @Override
    public void deleteFilm(long id) {
        films.remove(id);
    }

    @Override
    public Film getFilmById(Long id) {
        Film film = films.get(id);
        if (film == null) {
            throw new NotFoundException("Фильм с id " + id + "не найден");
        }
        return film;
    }

    public Long getNextId() {
        Long newId = films.keySet()
                .stream()
                .max(Long::compare)
                .orElse(0L);
        return newId + 1;
    }
}
