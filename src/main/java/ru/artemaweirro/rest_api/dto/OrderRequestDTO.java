package ru.artemaweirro.rest_api.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDTO {

    @NotEmpty(message = "Список bookIds не может быть пустым")
    private List<Long> bookIds;
}
