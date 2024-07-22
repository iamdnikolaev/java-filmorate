package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;
import java.util.Set;

/**
 * Фильм для коллекции.
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"name", "releaseDate"})
@AllArgsConstructor
@Builder
public class Film {

    /**
     * Поле идентификатор.
     */
    private Long id;

    /**
     * Поле названия.
     */
    @NotNull(message = "Название не может быть пустым.")
    @NotBlank(message = "Название не может быть пустым.")
    private String name;

    /**
     * Поле описания.
     */
    @Size(max = 200, message = "Максимальная длина описания - 200 символов.")
    private String description;

    /**
     * Поле даты релиза (выхода).
     */
    @NotNull(message = "Дата релиза не может быть пустой.")
    private LocalDate releaseDate;

    /**
     * Поле продолжительности.
     */
    @Positive(message = "Продолжительность фильма должна быть положительным числом.")
    private Integer duration;

    /**
     * Поле оценок "нравится" с хранением id проголосовавших пользователей.
     */
    private Set<Long> likesUserId;
}
