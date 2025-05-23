package ru.artemaweirro.rest_api.repositories;

import ru.artemaweirro.rest_api.models.Book;
import ru.artemaweirro.rest_api.models.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUserId(Long userId);
}
