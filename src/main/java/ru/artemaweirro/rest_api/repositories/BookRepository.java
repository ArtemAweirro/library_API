package ru.artemaweirro.rest_api.repositories;

import ru.artemaweirro.rest_api.models.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findByTitleContainingIgnoreCase(String title);

}