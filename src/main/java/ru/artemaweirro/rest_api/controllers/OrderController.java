package ru.artemaweirro.rest_api.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import ru.artemaweirro.rest_api.dto.OrderDTO;
import ru.artemaweirro.rest_api.dto.OrderInfoDTO;
import ru.artemaweirro.rest_api.dto.OrderRequestDTO;
import ru.artemaweirro.rest_api.mappers.OrderMapper;
import ru.artemaweirro.rest_api.models.Book;
import ru.artemaweirro.rest_api.models.Order;
import ru.artemaweirro.rest_api.models.Role;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.repositories.BookRepository;
import ru.artemaweirro.rest_api.repositories.OrderRepository;
import ru.artemaweirro.rest_api.repositories.UserRepository;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/orders/")
public class OrderController {
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    private final OrderMapper orderMapper;

    @Autowired
    public OrderController(OrderRepository orderRepository,
                           UserRepository userRepository,
                           BookRepository bookRepository,
                           OrderMapper orderMapper) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.bookRepository = bookRepository;
        this.orderMapper = orderMapper;
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


    @Operation(
            summary = "Получить все заказы (только для управляющих)",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Заказы получены",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    )
            }
    )
    @GetMapping
    public ResponseEntity<?> getAllOrders(Principal principal) {
        User currentUser = getCurrentUser(principal);

        boolean isAdminOrModerator = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MODERATOR;

        List<Order> orders = isAdminOrModerator
                ? orderRepository.findAll()
                : orderRepository.findByUserId(currentUser.getId());

        List<OrderInfoDTO> orderDTOs = orders.stream()
                .map(orderMapper::toInfoDto)
                .toList();

        return ResponseEntity.ok(orderDTOs);
    }


    @Operation(
            summary = "Получить заказ по id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Заказ получен",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Заказ не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ не найден\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "У пользователя нет доступа к заказу",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ запрещён: это не ваш заказ\"}"))
                    )
            }
    )
    @GetMapping("{id}/")
    public ResponseEntity<?> getOrderById(@PathVariable Long id, Principal principal) {
        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Заказ не найден"));
        }

        Order order = orderOpt.get();

        User currentUser = getCurrentUser(principal);

        boolean isAdminOrModerator = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MODERATOR;
        boolean isOwner = order.getUser().getId().equals(currentUser.getId());

        if (!isAdminOrModerator && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Доступ запрещён: это не ваш заказ"));
        }

        return ResponseEntity.ok(orderMapper.toInfoDto(order));
    }


    @Operation(
            summary = "Найти заказы текущего пользователя",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Заказы получены",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизирован",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Вы не авторизованы для выполнения этого действия\"}"))
                    ),
            }
    )
    @GetMapping("my/")
    public ResponseEntity<?> getMyOrders(Principal principal) {
        User currentUser = getCurrentUser(principal);

        List<Order> userOrders = orderRepository.findByUserId(currentUser.getId());

        List<OrderInfoDTO> orderDTOs = userOrders.stream()
                .map(orderMapper::toInfoDto)
                .toList();

        return ResponseEntity.ok(orderDTOs);
    }


    @Operation(
            summary = "Оформить новый заказ",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "Заказ оформлен",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "Пользователь не авторизирован",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"Вы не авторизованы для выполнения этого действия\"}"))
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
    public ResponseEntity<?> createOrder(@RequestBody @Valid OrderRequestDTO orderRequestDTO, Principal principal) {
        User currentUser = getCurrentUser(principal);
        List<Book> books = bookRepository.findAllById(orderRequestDTO.getBookIds());
        if (books.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Collections.singletonMap("message", "Книги не найдены"));
        }
        // Подсчитываем общую цену
        double totalPrice = books.stream()
                .mapToDouble(Book::getPrice)
                .sum();

        Order order = new Order();
        order.setUser(currentUser);
        order.setBooks(books);
        order.setTotalPrice(totalPrice);

        orderRepository.save(order);

        return ResponseEntity.status(HttpStatus.CREATED).body(orderMapper.toInfoDto(order));
    }


    @Operation(
            summary = "Полностью изменить заказ",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "Заказ получен",
                            content = @Content(schema = @Schema(implementation = OrderDTO.class))
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "Пользователь/Книги не найдены",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Пользователь/Книги не найдены\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Заказ не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ не найден\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "У пользователя нет доступа ко всем (кроме своих) заказам",
                            content = @Content(schema = @Schema(
                                    example = "{\"error\": \"У вас нет прав на выполнение этого действия\"}"))
                    )
            }
    )
    @PutMapping("{id}/")
    public ResponseEntity<?> updateOrder(@PathVariable Long id, @Valid @RequestBody OrderRequestDTO orderRequestDTO, Principal principal) {
        User currentUser = getCurrentUser(principal);

        Optional<Order> orderOpt = orderRepository.findById(id);
        if (orderOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Заказ не найден"));
        }
        boolean isAdminOrModerator = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MODERATOR;
        boolean isOwner = orderOpt.get().getUser().getId().equals(currentUser.getId());
        if (!isAdminOrModerator && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Доступ запрещён: это не ваш заказ"));
        }

        Optional<User> userOpt = userRepository.findById(orderOpt.get().getUser().getId());
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Пользователь не найден"));
        }

        List<Book> books = bookRepository.findAllById(orderRequestDTO.getBookIds());
        if (books.isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Книги не найдены"));
        }

        Order order = orderOpt.get();
        order.setUser(userOpt.get());
        order.setBooks(books);
        order.setTotalPrice(books.stream()
                .mapToDouble(Book::getPrice)
                .sum());
        orderRepository.save(order);

        return ResponseEntity.ok(orderMapper.toDto(order));
    }


    @Operation(
            summary = "Удалить заказ",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "Заказ удален"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "У пользователя нет доступа к заказу",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Доступ запрещён: это не ваш заказ\"}"))
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "Заказ не найден",
                            content = @Content(schema = @Schema(example = "{\"error\": \"Заказ не найден\"}"))
                    )
            }
    )
    @DeleteMapping("{id}/")
    public ResponseEntity<?> deleteOrder(@PathVariable Long id, Principal principal) {
        Optional<Order> optionalOrder = orderRepository.findById(id);
        if (optionalOrder.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Collections.singletonMap("error", "Заказ не найден"));
        }

        User currentUser = getCurrentUser(principal);

        Order order = optionalOrder.get();

        boolean isAdminOrModerator = currentUser.getRole() == Role.ADMIN || currentUser.getRole() == Role.MODERATOR;
        boolean isOwner = order.getUser().getId().equals(currentUser.getId());

        if (!isAdminOrModerator && !isOwner) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("error", "Доступ запрещён: это не ваш заказ"));
        }

        orderRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

}
