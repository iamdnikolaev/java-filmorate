package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

/**
 * Пользователь сервиса.
 *
 * @version 1.0
 * @author Николаев Д.В.
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(of = {"login"})
@AllArgsConstructor
@Builder
public class User {
    /**
     * Поле идентификатор.
     */
    private Long id;

    /**
     * Поле адрес электронной почты.
     */
    @NotNull(message = "Электронная почта не может быть пустой и должна содержать символ @.")
    @NotBlank(message = "Электронная почта не может быть пустой и должна содержать символ @.")
    @Email(message = "Электронная почта не может быть пустой и должна содержать символ @.")
    private String email;

    /**
     * Поле логин.
     */
    @NotNull(message = "Логин не может быть пустым и содержать пробелы.")
    @NotBlank(message = "Логин не может быть пустым и содержать пробелы.")
    private String login;

    /**
     * Поле с именем для отображения.
     */
    private String name;

    /**
     * Поле даты рождения.
     */
    @Past(message = "Дата рождения не может быть в будущем.")
    private LocalDate birthday;
}
