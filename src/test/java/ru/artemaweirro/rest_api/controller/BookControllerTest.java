package ru.artemaweirro.rest_api.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import ru.artemaweirro.rest_api.dto.BookDTO;
import ru.artemaweirro.rest_api.models.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;
import ru.artemaweirro.rest_api.controllers.BookController;
import ru.artemaweirro.rest_api.mappers.BookMapper;
import ru.artemaweirro.rest_api.repositories.BookRepository;
import ru.artemaweirro.rest_api.repositories.UserRepository;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookMapper bookMapper;

    private BookController bookController;

    @BeforeEach
    void setUp() {
        bookController = new BookController(bookRepository, userRepository, bookMapper);
    }

    @Test
    void testGetAllBooks() {
        Book book_first = new Book();
        book_first.setId(1L);
        book_first.setTitle("First Book");

        Book book_second = new Book();
        book_second.setId(2L);
        book_second.setTitle("Second Book");

        BookDTO dto1 = new BookDTO();
        dto1.setId(1L);
        dto1.setTitle("First Book");

        BookDTO dto2 = new BookDTO();
        dto2.setId(2L);
        dto2.setTitle("Second Book");

        List<Book> expectedBooks = List.of(book_first, book_second);

        // Мокаем вызов репозитория
        when(bookRepository.findAll()).thenReturn(expectedBooks);

        // Вызываем контроллер
        List<Book> actualBooks = bookController.getAllBooks();

        // Проверяем, что вернулся именно тот список
        assertEquals(expectedBooks, actualBooks);
        // Дополнительно проверить размер
        assertEquals(2, actualBooks.size());
        // Можно проверить поля первой книги
        assertEquals(1L, actualBooks.get(0).getId());
        assertEquals("First Book", actualBooks.get(0).getTitle());
        // Проверяем, что репозиторий был вызван ровно один раз
        verify(bookRepository, times(1)).findAll();
    }

    @Test
    void testGetBookById_BookExists() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test");

        BookDTO dto = new BookDTO();
        dto.setId(1L);
        dto.setTitle("Test");

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookMapper.toDto(book)).thenReturn(dto);

        ResponseEntity<?> response = bookController.getBookById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testGetBookById_BookNotFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = bookController.getBookById(1L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testUpdateBook_Success() {
        Long id = 1L;

        Book book = new Book();
        book.setId(id);
        book.setTitle("Old title");

        BookDTO updateDto = new BookDTO();
        updateDto.setTitle("New title");

        BookDTO updatedDto = new BookDTO();
        updatedDto.setTitle("New title");

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        // При обновлении сущности из DTO
        doAnswer(invocation -> {
            BookDTO dto = invocation.getArgument(0);
            Book entity = invocation.getArgument(1);
            entity.setTitle(dto.getTitle()); // эмулируем работу маппера
            return null;
        }).when(bookMapper).updateEntityFromDto(updateDto, book);

        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDto(book)).thenReturn(updatedDto);

        ResponseEntity<?> response = bookController.updateBook(id, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(updatedDto, response.getBody());

        verify(bookRepository).findById(id);
        verify(bookMapper).updateEntityFromDto(updateDto, book);
        verify(bookRepository).save(book);
        verify(bookMapper).toDto(book);
    }

    @Test
    void testUpdateBook_NotFound() {
        Long id = 1L;
        BookDTO updateDto = new BookDTO();
        updateDto.setTitle("New title");

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = bookController.updateBook(id, updateDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Книга не найдена", body.get("error"));

        verify(bookRepository).findById(id);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    void testUpdateBookPartially_Success() {
        Long id = 1L;
        Book book = new Book();
        book.setId(id);
        book.setTitle("Old title");
        book.setAuthor("Old author");

        BookDTO updateDto = new BookDTO();
        updateDto.setTitle("New title");
        // остальные поля null, обновляем частично только title

        when(bookRepository.findById(id)).thenReturn(Optional.of(book));
        when(bookRepository.save(book)).thenReturn(book);

        ResponseEntity<Object> response = bookController.updateBookPartially(id, updateDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        Book updatedBook = (Book) response.getBody();
        assertNotNull(updatedBook);
        assertEquals("New title", updatedBook.getTitle());
        assertEquals("Old author", updatedBook.getAuthor()); // не поменялось

        verify(bookRepository).findById(id);
        verify(bookRepository).save(book);
    }

    @Test
    void testUpdateBookPartially_EmptyFields() {
        Long id = 1L;
        BookDTO emptyDto = new BookDTO(); // все поля null

        ResponseEntity<Object> response = bookController.updateBookPartially(id, emptyDto);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Нужно указать хотя бы одно поле для обновления", body.get("error"));

        verifyNoInteractions(bookRepository, bookMapper);
    }

    @Test
    void testUpdateBookPartially_NotFound() {
        Long id = 1L;
        BookDTO updateDto = new BookDTO();
        updateDto.setTitle("New title");

        when(bookRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<Object> response = bookController.updateBookPartially(id, updateDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertInstanceOf(Map.class, response.getBody());

        Map<?, ?> body = (Map<?, ?>) response.getBody();
        assertEquals("Книга не найдена", body.get("error"));

        verify(bookRepository).findById(id);
        verifyNoMoreInteractions(bookRepository, bookMapper);
    }

    @Test
    void testDeleteBook_Success() {
        Long id = 1L;

        when(bookRepository.existsById(id)).thenReturn(true);
        doNothing().when(bookRepository).deleteById(id);

        ResponseEntity<?> response = bookController.deleteBook(id);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        verify(bookRepository).existsById(id);
        verify(bookRepository).deleteById(id);
    }

    @Test
    void testDeleteBook_NotFound() {
        Long id = 1L;

        when(bookRepository.existsById(id)).thenReturn(false);

        ResponseEntity<?> response = bookController.deleteBook(id);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        verify(bookRepository).existsById(id);
        verify(bookRepository, never()).deleteById(any());
    }

}
