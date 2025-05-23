package ru.artemaweirro.rest_api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import ru.artemaweirro.rest_api.models.Role;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private Long id;

    @NotBlank(message = "Имя пользователя не может быть пустым")
    private String username;

    @NotBlank(message = "Пароль не может быть пустым")
    private String password;

    @NotBlank(message = "Роль не может быть пустой")
    private String role;

}