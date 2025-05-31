package ru.artemaweirro.rest_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.artemaweirro.rest_api.controllers.OrderController;
import ru.artemaweirro.rest_api.dto.OrderInfoDTO;
import ru.artemaweirro.rest_api.dto.OrderRequestDTO;
import ru.artemaweirro.rest_api.mappers.OrderMapper;
import ru.artemaweirro.rest_api.models.Book;
import ru.artemaweirro.rest_api.models.Order;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.models.Role;
import ru.artemaweirro.rest_api.repositories.BookRepository;
import ru.artemaweirro.rest_api.repositories.OrderRepository;
import ru.artemaweirro.rest_api.repositories.UserRepository;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    @Mock
    private OrderRepository orderRepository;
    @Mock
    private BookRepository bookRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private OrderMapper orderMapper;

    private OrderController orderController;

    private final User adminUser = new User();
    private final User regularUser = new User();
    private final Principal adminPrincipal = () -> "admin";
    private final Principal userPrincipal = () -> "user";

    @BeforeEach
    void setUp() {
        orderController = new OrderController(orderRepository, userRepository, bookRepository, orderMapper);

        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);

        regularUser.setId(2L);
        regularUser.setUsername("user");
        regularUser.setRole(Role.USER);
    }

    // ----------- getAllOrders -----------
    @Test
    void testGetAllOrders_asAdmin() {
        List<Order> orders = List.of(new Order(), new Order());
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));
        when(orderRepository.findAll()).thenReturn(orders);
        when(orderMapper.toInfoDto(any())).thenReturn(new OrderInfoDTO());

        ResponseEntity<?> response = orderController.getAllOrders(adminPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderRepository).findAll();
    }

    @Test
    void testGetAllOrders_asUser() {
        List<Order> userOrders = List.of(new Order());
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(orderRepository.findByUserId(regularUser.getId())).thenReturn(userOrders);
        when(orderMapper.toInfoDto(any())).thenReturn(new OrderInfoDTO());

        ResponseEntity<?> response = orderController.getAllOrders(userPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(orderRepository).findByUserId(regularUser.getId());
    }

    // ----------- getOrderById -----------
    @Test
    void testGetOrderById_asOwner() {
        Order order = new Order();
        order.setUser(regularUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(orderMapper.toInfoDto(order)).thenReturn(new OrderInfoDTO());

        ResponseEntity<?> response = orderController.getOrderById(1L, userPrincipal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void testGetOrderById_notFound() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = orderController.getOrderById(1L, userPrincipal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testGetOrderById_forbidden() {
        Order order = new Order();
        order.setUser(adminUser); // Заказы администратора
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));

        // Запрашиваем заказ как пользователь
        ResponseEntity<?> response = orderController.getOrderById(1L, userPrincipal);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

    // ----------- createOrder -----------
    @Test
    void testCreateOrder_success() {
        OrderRequestDTO request = new OrderRequestDTO(List.of(1L, 2L));

        Book book_first = new Book();
        book_first.setId(1L);
        book_first.setTitle("First Book");

        Book book_second = new Book();
        book_second.setId(2L);
        book_second.setTitle("Second Book");

        List<Book> books = List.of(book_first, book_second);
        Order savedOrder = new Order();
        savedOrder.setBooks(books);
        savedOrder.setUser(regularUser);
        savedOrder.setTotalPrice(300);

        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(bookRepository.findAllById(request.getBookIds())).thenReturn(books);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        when(orderMapper.toInfoDto(any(Order.class))).thenReturn(new OrderInfoDTO());

        ResponseEntity<?> response = orderController.createOrder(request, userPrincipal);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
    }

    @Test
    void testCreateOrder_booksNotFound() {
        OrderRequestDTO request = new OrderRequestDTO(List.of(1L, 2L));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));
        when(bookRepository.findAllById(request.getBookIds())).thenReturn(Collections.emptyList());

        ResponseEntity<?> response = orderController.createOrder(request, userPrincipal);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
    }

    // ----------- deleteOrder -----------
    @Test
    void testDeleteOrder_asAdmin() {
        Order order = new Order();
        order.setUser(regularUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("admin")).thenReturn(Optional.of(adminUser));

        ResponseEntity<?> response = orderController.deleteOrder(1L, adminPrincipal);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(orderRepository).deleteById(1L);
    }

    @Test
    void testDeleteOrder_notFound() {
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = orderController.deleteOrder(999L, userPrincipal);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDeleteOrder_forbidden() {
        Order order = new Order();
        order.setUser(adminUser);
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(userRepository.findByUsername("user")).thenReturn(Optional.of(regularUser));

        ResponseEntity<?> response = orderController.deleteOrder(1L, userPrincipal);

        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
    }

}
