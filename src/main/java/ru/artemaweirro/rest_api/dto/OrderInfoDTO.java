package ru.artemaweirro.rest_api.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;
import ru.artemaweirro.rest_api.models.Book;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoDTO {
    private Long id;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;

    private String user;

    private List<Book> books;

    private Double totalPrice;
}
