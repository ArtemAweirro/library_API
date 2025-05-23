package ru.artemaweirro.rest_api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BookDTO {
    private Long id;

    @NotBlank(message = "Название книги обязательно")
    private String title;

    @NotBlank(message = "Автор книги обязателен")
    private String author;

    @NotNull(message = "Цена обязательна")
    @DecimalMin(value = "1.0", message = "Цена должна быть не менее 1")
    private Double price;

    @NotBlank(message = "Описание книги обязательно")
    private String description;
}
