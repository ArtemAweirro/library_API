package ru.artemaweirro.rest_api.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Обработка 404 только для /api/**
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<?> handleNotFound(HttpServletRequest request, NoHandlerFoundException ex) throws NoHandlerFoundException {
        if (request.getRequestURI().startsWith("/api/")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Такой страницы не существует"));
        }

        // Для обычных HTML-запросов — Spring сам обрабатывает
        throw ex;
    }

    // Обработка 405 только для /api/**
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<?> handleMethodNotAllowed(HttpServletRequest request, HttpRequestMethodNotSupportedException ex) throws HttpRequestMethodNotSupportedException {
        if (request.getRequestURI().startsWith("/api/")) {
            String method = ex.getMethod();
            return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
                    .body(Map.of("error", "Метод " + method + " не разрешен для этого пути"));
        }

        // Для обычных HTML-запросов — Spring сам обрабатывает
        throw ex;
    }
}