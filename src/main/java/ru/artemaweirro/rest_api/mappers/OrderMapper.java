package ru.artemaweirro.rest_api.mappers;

import org.springframework.stereotype.Component;
import ru.artemaweirro.rest_api.dto.BookDTO;
import ru.artemaweirro.rest_api.dto.OrderDTO;
import ru.artemaweirro.rest_api.dto.OrderInfoDTO;
import ru.artemaweirro.rest_api.models.Book;
import ru.artemaweirro.rest_api.models.Order;

import java.util.stream.Collectors;

@Component
public class OrderMapper {

    public OrderDTO toDto(Order order) {
        OrderDTO dto = new OrderDTO();
        dto.setId(order.getId());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setUserId(order.getUser().getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setBookIds(order.getBooks().stream()
                .map(Book::getId)
                .collect(Collectors.toList()));
        return dto;
    }


    public OrderInfoDTO toInfoDto(Order order) {
        OrderInfoDTO infoDTO = new OrderInfoDTO();
        infoDTO.setId(order.getId());
        infoDTO.setCreatedAt(order.getCreatedAt());
        infoDTO.setUser(order.getUser().getUsername());
        infoDTO.setBooks(order.getBooks());
        infoDTO.setTotalPrice(order.getTotalPrice());
        return infoDTO;
    }
}
