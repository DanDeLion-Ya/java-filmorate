package ru.yandex.practicum.filmorate.storage.film;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import(FilmDbStorage.class)
public class FilmDbStorageTest {
    private final FilmDbStorage filmStorage;
    private Film film;

    @BeforeEach
    void setUp() {
        Mpa mpa = new Mpa();
        mpa.setId(1L);
        mpa.setName("16+");

        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Комедия");

        film = new Film();
//        НЕТ
        film.setName("Самый лучший фильм");
        film.setDescription("Фильм-пародия от команды юмористов, главную роль исполняет тот же актёр, " +
                "что и главную роль в фильме 'Колобок'. Фильм так себе, по мнению описывающего описание. ");
        film.setReleaseDate(LocalDate.of(2008,1,24));
        film.setDuration(105);
        film.setMpa(mpa);
        film.setGenres(List.of(genre));
    }

    @Test
    public void testCreateFilm() {
       Film createdFilm = filmStorage.createFilm(film);
        assertThat(createdFilm).isNotNull();
        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo(film.getName());
        assertThat(createdFilm.getDescription()).isEqualTo(film.getDescription());
        assertThat(createdFilm.getReleaseDate()).isEqualTo(film.getReleaseDate());
        assertThat(createdFilm.getDuration()).isEqualTo(film.getDuration());
        assertThat(createdFilm.getMpa().getId()).isEqualTo(film.getMpa().getId());
        assertThat(createdFilm.getGenres().get(0).getName()).isEqualTo(film.getGenres().get(0).getName());
    }

    @Test
    public void testGetFilmById() {
        Film createdFilm = filmStorage.createFilm(film);
        Film findFilm = filmStorage.getFilmById(createdFilm.getId());
        assertThat(findFilm).isNotNull();
        assertThat(findFilm.getId()).isEqualTo(createdFilm.getId());

        assertThat(findFilm.getGenres()).isNotEmpty();
        assertThat(findFilm.getGenres().get(0).getName()).isEqualTo(createdFilm.getGenres().get(0).getName());
    }

    @Test
    public void testUpdateFilm() {
        Genre newGenre = new Genre();
        newGenre.setId(2L);
        newGenre.setName("Документальный");

        Film createdFilm = filmStorage.createFilm(film);
        createdFilm.setName("ТестоФильм");
        createdFilm.setDescription("Тестовое небольшое описание");
        createdFilm.setDuration(200);
        createdFilm.setGenres(List.of(newGenre));

        Film updatedFilm = filmStorage.updateFilm(createdFilm);
        assertThat(updatedFilm).isNotNull();
        assertThat(updatedFilm.getId()).isEqualTo(createdFilm.getId());
        assertThat(updatedFilm.getName()).isEqualTo("ТестоФильм");
        assertThat(updatedFilm.getDescription()).isEqualTo("Тестовое небольшое описание");
        assertThat(updatedFilm.getReleaseDate()).isEqualTo(createdFilm.getReleaseDate());
        assertThat(updatedFilm.getDuration()).isEqualTo(200);
        assertThat(updatedFilm.getMpa()).isEqualTo(createdFilm.getMpa());
        assertThat(updatedFilm.getGenres().get(0).getId()).isEqualTo(2);
        assertThat(updatedFilm.getGenres().get(0).getName()).isEqualTo("Документальный");
    }

    @Test
    public void testDeleteFilm() {
        Film createdFilm = filmStorage.createFilm(film);
        filmStorage.deleteFilm(createdFilm.getId());
        Film userFromDb = filmStorage.getFilmById(createdFilm.getId());
        assertThat(userFromDb).isNull();
    }

    @Test
    public void testGetAllFilms() {
        Film film1 = filmStorage.createFilm(film);

        Mpa mpa2 = new Mpa();
        mpa2.setId(2L);
        mpa2.setName("16+");

        Genre genre2 = new Genre();
        genre2.setId(1L);
        genre2.setName("Научная фантастика");

        Mpa mpa3 = new Mpa();
        mpa3.setId(3L);
        mpa3.setName("16+");

        Genre genre3 = new Genre();
        genre3.setId(1L);
        genre3.setName("Боевик");

        Film film2 = new Film();
        film2.setName("Франкенштейн");
        film2.setDescription("Оно живое.");
        film2.setReleaseDate(LocalDate.of(1931,11,21));
        film2.setDuration(71);
        film2.setMpa(mpa2);
        film2.setGenres(List.of(genre2));
        filmStorage.createFilm(film2);

        Film film3 = new Film();
        film3.setName("300 спартанцев");
        film3.setDescription("Это СПААААРТАААААААА. ");
        film3.setReleaseDate(LocalDate.of(2007,3,9));
        film3.setDuration(117);
        film3.setMpa(mpa3);
        film3.setGenres(List.of(genre3));
        filmStorage.createFilm(film3);

        List<Film> allFilms = filmStorage.getAllFilms();
        assertThat(allFilms.size()).isEqualTo(3);
    }
}
