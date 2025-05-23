package ru.artemaweirro.rest_api.models;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity // говорит о том, что это сущность в БД
@Table(name = "books") // имя таблицы в БД
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title; // Название книги
    private String author; // Автор книги
    private double price; // Цена книги
    private String description; // Описание книги

}
