package ru.artemaweirro.rest_api.mappers;

import org.springframework.stereotype.Component;
import ru.artemaweirro.rest_api.dto.UserInfoDTO;
import ru.artemaweirro.rest_api.models.Role;
import ru.artemaweirro.rest_api.models.User;

@Component
public class UserMapper {
    public UserInfoDTO toDto(User user) {
        if (user == null) return null;

        UserInfoDTO dto = new UserInfoDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setRole(user.getRole());
        return dto;
    }

    public Role defineRole(String nameRole) {
        try {
            return Role.valueOf(nameRole.toUpperCase());
        } catch (IllegalArgumentException | NullPointerException e) {
            return Role.USER; // значение по умолчанию
        }
    }

    public void updateEntityFromDTO(UserInfoDTO dto, User entity) {
        entity.setUsername(dto.getUsername());
        entity.setRole(defineRole(dto.getRole().name()));
    }
}
