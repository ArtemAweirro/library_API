package ru.artemaweirro.rest_api.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.artemaweirro.rest_api.models.Role;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoDTO {
    private Long id;
    private String username;
    private Role role;
}
