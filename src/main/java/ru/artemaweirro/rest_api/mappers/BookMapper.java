package ru.artemaweirro.rest_api.mappers;

import org.springframework.stereotype.Component;
import ru.artemaweirro.rest_api.dto.BookDTO;
import ru.artemaweirro.rest_api.models.Book;

@Component
public class BookMapper {

    public BookDTO toDto(Book book) {
        BookDTO dto = new BookDTO();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPrice(book.getPrice());
        dto.setDescription(book.getDescription());
        return dto;
    }

    public Book toEntity(BookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPrice(dto.getPrice());
        book.setDescription(dto.getDescription());
        return book;
    }

    public void updateEntityFromDto(BookDTO dto, Book entity) {
        entity.setTitle(dto.getTitle());
        entity.setAuthor(dto.getAuthor());
        entity.setPrice(dto.getPrice());
        entity.setDescription(dto.getDescription());
    }

}
