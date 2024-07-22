package ru.yandex.practicum.filmorate.controller;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Адаптер типа LocalDate для обработки сериализации и десериализации поля даты}
 *
 * @author Николаев Д.В.
 * @version 1.0
 */
public class LocalDateTypeAdapter extends TypeAdapter<LocalDate> {
    /**
     * Поле используемого формата даты и времени
     */
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    /**
     * Метод записи в формате JSON
     *
     * @param jsonWriter объект писателя
     * @param localDate  записываемое значение даты
     */
    @Override
    public void write(final JsonWriter jsonWriter, final LocalDate localDate) throws IOException {
        if (localDate == null) {
            jsonWriter.nullValue();
        } else {
            jsonWriter.value(localDate.format(timeFormatter));
        }
    }

    /**
     * Метод чтения в формате JSON, чтобы распарсить объект класса LocalDate
     *
     * @param jsonReader объект читателя
     */
    @Override
    public LocalDate read(final JsonReader jsonReader) throws IOException {
        if (jsonReader.peek() == JsonToken.NULL) {
            jsonReader.nextNull();
            return null;
        } else {
            return LocalDate.parse(jsonReader.nextString(), timeFormatter);
        }
    }
}
