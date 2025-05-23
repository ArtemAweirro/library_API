package ru.artemaweirro.rest_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.artemaweirro.rest_api.dto.UserDTO;
import ru.artemaweirro.rest_api.dto.UserInfoDTO;
import ru.artemaweirro.rest_api.mappers.UserMapper;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.repositories.UserRepository;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/users/")
public class UserController {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserController(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Operation(summary = "Получить список всех пользователей")
    @GetMapping
    public List<UserInfoDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(userMapper::toDto)
                .toList();
    }

    @Operation(
            summary = "Вернуть информацию о текущем пользователе",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Вывод информации о пользователе",
                            content = @Content(schema = @Schema(implementation = UserInfoDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не аутентифицирован",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Пользователь не аутентифицирован\"}"))
                    )
            }
    )
    @GetMapping("me/")
    public ResponseEntity<?> getCurrentUserInfo(Principal principal) {
        if (principal == null || principal.getName() == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Пользователь не аутентифицирован"));
        }

        String username = principal.getName();
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty())
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Пожалуйста, пройдите авторизацию заново"));
        return ResponseEntity.ok(userMapper.toDto(userOpt.get()));
    }

    @Operation(
            summary = "Получить пользователя по id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Вывод информации о пользователе",
                            content = @Content(schema = @Schema(implementation = UserInfoDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Пользователь не найден\"}"))
                    )
            }
    )
    @GetMapping("{id}/")
    public ResponseEntity<?> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> ResponseEntity.ok(userMapper.toDto(user)))
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body(Map.of("error", "Пользователь не найден")));
    }

    @Operation(
            summary = "Изменить полностью пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Вывод информации о пользователе",
                            content = @Content(schema = @Schema(implementation = UserInfoDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Пользователь не найден\"}"))
                    )
            }
    )
    @PatchMapping("{id}/")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @Valid @RequestBody UserInfoDTO updatedUser) {
        return userRepository.findById(id)
                .<ResponseEntity<?>>map(user -> {
                    userMapper.updateEntityFromDTO(updatedUser, user);
                    userRepository.save(user); // сохраняем обновлённую книгу
                    return ResponseEntity.ok(userMapper.toDto(user)); // возвращаем DTO
                })
            .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Пользователь не найден")));
    }

    @Operation(
            summary = "Удалить пользователя (только для администратора)",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Пользователь удален"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Пользователь не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ не найден\"}"))
                    )
            }
    )
    @DeleteMapping("{id}/")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        Optional<User> optionalUser = userRepository.findById(id);
        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "Пользователь не найден"));
        }

        userRepository.delete(optionalUser.get());
        return ResponseEntity.noContent().build();
    }

}
