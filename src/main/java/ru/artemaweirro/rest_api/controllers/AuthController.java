package ru.artemaweirro.rest_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.artemaweirro.rest_api.dto.AuthRequest;
import ru.artemaweirro.rest_api.dto.AuthResponse;
import ru.artemaweirro.rest_api.models.Role;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.repositories.UserRepository;
import ru.artemaweirro.rest_api.security.JwtTokenProvider;

import java.util.Collections;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthenticationManager authenticationManager,
                          JwtTokenProvider jwtTokenProvider,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Operation(
            summary = "Получение JWT-токена зарегистрированного пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Пользователь зарегестрирован",
                            content = @Content(schema = @Schema(
                                    example = "{\"token\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhcnRlbWF3ZWlycm8iLCJyb2xlIjoiTU9ERVJBVE9SIiwiaWF0IjoxNzQ3ODQ1NjE0LCJleHAiOjE3NDc4NDkyMTR9.gRCKc5vWcY3jjSa_FO9gHAdeoxy1X6UzrCWFimuvEWs\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Пользователь с таким именем уже зарегистрирован",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Пользователь с таким именем уже зарегистрирован\"}"))
                    )
            }
    )
    @PostMapping("login/")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
            );

            User user = userRepository.findByUsername(request.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));

            String token = jwtTokenProvider.createToken(user.getUsername(), user.getRole().name());
            return ResponseEntity.ok(new AuthResponse(token));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Collections.singletonMap("error", "Неверный логин или пароль"));
        }
    }

    @Operation(
            summary = "Регистрация пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Пользователь зарегестрирован",
                            content = @Content(schema = @Schema(implementation = AuthResponse.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Пользователь с таким именем уже зарегистрирован",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Пользователь с таким именем уже зарегистрирован\"}"))
                    )
            }
    )
    @PostMapping("register/")
    public ResponseEntity<?> register(@RequestBody AuthRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Пользователь с таким именем уже зарегистрирован"));
        }

        User newUser = new User();
        newUser.setUsername(request.getUsername());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setRole(Role.USER); // назначаем базовую роль

        userRepository.save(newUser);

        String token = jwtTokenProvider.createToken(newUser.getUsername(), newUser.getRole().name());
        return ResponseEntity.status(HttpStatus.CREATED).body(new AuthResponse(token));
    }
}