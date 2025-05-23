package ru.artemaweirro.rest_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.artemaweirro.rest_api.dto.BookDTO;
import ru.artemaweirro.rest_api.mappers.BookMapper;
import ru.artemaweirro.rest_api.models.Book;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.repositories.BookRepository;
import ru.artemaweirro.rest_api.repositories.UserRepository;

import java.security.Principal;
import java.util.*;

@RestController
@RequestMapping("/api/books/")
public class BookController {
    private final BookRepository bookRepository;
    private final UserRepository userRepository;
    private final BookMapper bookMapper;

    public BookController(BookRepository bookRepository, UserRepository userRepository, BookMapper bookMapper) {
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.bookMapper = bookMapper;
    }

    // Идентификация пользователя, сделавшего запрос
    public User getCurrentUser(Principal principal) {
        if (principal == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не авторизован");
        }

        String username = principal.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Пользователь не найден"));
    }


    @Operation(summary = "Получить список книг")
    @GetMapping
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Operation(
            summary = "Получить книгу по id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Книга найдена",
                            content = @Content(schema = @Schema(implementation = BookDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Книга не найдена\"}"))
                    )
            }
    )
    @GetMapping("{id}/")
    public ResponseEntity<?> getBookById(@PathVariable Long id) {
        return bookRepository.findById(id)
                .<ResponseEntity<?>>map(book -> ResponseEntity.ok(bookMapper.toDto(book)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Книга не найдена")));
    }

    @Operation(
            summary = "Добавить новую книгу",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Книга создана",
                            content = @Content(schema = @Schema(implementation = BookDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Тело запроса отсутствует или некорректно",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Тело запроса отсутствует или некорректно\"}"))
                    )
            }
    )
    @PostMapping
    public Book addBook(@Valid @RequestBody BookDTO bookDTO) {
        Book book = bookMapper.toEntity(bookDTO);
        return bookRepository.save(book);
    }

    @Operation(
            summary = "Полностью изменить книгу",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Книга отредактирована",
                            content = @Content(schema = @Schema(implementation = BookDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Книга не найдена\"}"))
                    )
            }
    )
    @PutMapping("{id}/")
    public ResponseEntity<?> updateBook(@PathVariable Long id, @Valid @RequestBody BookDTO updateBookDTO) {
        return bookRepository.findById(id)
                .<ResponseEntity<?>>map(book -> {
                    bookMapper.updateEntityFromDto(updateBookDTO, book);
                    bookRepository.save(book); // сохраняем обновлённую книгу
                    return ResponseEntity.ok(bookMapper.toDto(book)); // возвращаем DTO
                })
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Книга не найдена"))
        );
    }

    @Operation(
            summary = "Частично изменить книгу",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Книга отредактирована",
                            content = @Content(schema = @Schema(implementation = BookDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Не указаны поля для обновления",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Нужно указать хотя бы одно поле для обновления\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Книга не найдена\"}"))
                    )
            }
    )
    @PatchMapping("{id}/")
    public ResponseEntity<Object> updateBookPartially(@PathVariable Long id, @RequestBody BookDTO bookDTO) {
        // Проверка тела запроса на пустоту
        if (bookDTO.getTitle() == null && bookDTO.getAuthor() == null &&
                bookDTO.getPrice() == null && bookDTO.getDescription() == null) {
            return ResponseEntity.badRequest()
                    .body(Collections.singletonMap("error", "Нужно указать хотя бы одно поле для обновления"));
        }

        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Книга не найдена"));
        }

        Book book = optionalBook.get();

        // Обновляем только те поля, которые не null
        if (bookDTO.getTitle() != null) {
            book.setTitle(bookDTO.getTitle());
        }
        if (bookDTO.getAuthor() != null) {
            book.setAuthor(bookDTO.getAuthor());
        }
        if (bookDTO.getPrice() != null) {
            book.setPrice(bookDTO.getPrice());
        }
        if (bookDTO.getDescription() != null) {
            book.setDescription(bookDTO.getDescription());
        }

        bookRepository.save(book);
        return ResponseEntity.ok(book);
    }


    @Operation(
            summary = "Удалить книгу",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Книга удалена"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Книга не найдена",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Книга не найдена\"}"))
                    )
            }
    )
    @DeleteMapping("{id}/")
    public ResponseEntity<?> deleteBook(@PathVariable Long id) {
        if (!bookRepository.existsById(id)) {
            // Проверяем наличие данной книги
            return ResponseEntity.notFound().build();
        }
        // Удаляем книгу
        bookRepository.deleteById(id);
        // Возвращаем ответ
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @Operation(
            summary = "Найти книги по названию",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Книги найдены",
                            content = @Content(schema = @Schema(implementation = BookDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Книг не найдено",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Книг с данным названием не найдено\"}"))
                    )
            }
    )
    @GetMapping("by-title/")
    public ResponseEntity<Object> getBooksByTitle(@RequestParam String title) {
        List<Book> books = bookRepository.findByTitleContainingIgnoreCase(title);
        if (books.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Книг с данным названием не найдено"));
        }
        return ResponseEntity.ok(books);
    }
}
