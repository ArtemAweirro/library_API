package ru.artemaweirro.rest_api.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.artemaweirro.rest_api.controllers.UserController;
import ru.artemaweirro.rest_api.dto.UserInfoDTO;
import ru.artemaweirro.rest_api.mappers.UserMapper;
import ru.artemaweirro.rest_api.models.Role;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.repositories.UserRepository;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    private UserController userController;

    private final User adminUser = new User();
    private final User regularUser = new User();

    @BeforeEach
    void setUp() {
        userController = new UserController(userRepository, userMapper);

        regularUser.setId(1L);
        regularUser.setUsername("user");
        regularUser.setRole(Role.USER);

        adminUser.setId(2L);
        adminUser.setUsername("admin");
        adminUser.setRole(Role.ADMIN);
    }

    // ---------- getAllUsers ----------
    @Test
    void testGetAllUsers() {
        List<User> users = List.of(regularUser, adminUser);

        when(userRepository.findAll()).thenReturn(users);
        when(userMapper.toDto(any(User.class))).thenReturn(new UserInfoDTO());

        List<UserInfoDTO> result = userController.getAllUsers();

        assertEquals(2, result.size());
        verify(userRepository).findAll();
        verify(userMapper, times(2)).toDto(any(User.class));
    }

    // ---------- getCurrentUserInfo ----------
    @Test
    void testGetCurrentUserInfo_success() {
        Principal principal = () -> "user1";
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setRole(Role.USER);

        UserInfoDTO dto = new UserInfoDTO();

        when(userRepository.findByUsername("user1")).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        ResponseEntity<?> response = userController.getCurrentUserInfo(principal);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testGetCurrentUserInfo_nullPrincipal() {
        ResponseEntity<?> response = userController.getCurrentUserInfo(null);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    @Test
    void testGetCurrentUserInfo_userNotFound() {
        Principal principal = () -> "ghost";
        when(userRepository.findByUsername("ghost")).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getCurrentUserInfo(principal);

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
    }

    // ---------- getUserById ----------
    @Test
    void testGetUserById_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("user1");
        user.setRole(Role.USER);

        UserInfoDTO dto = new UserInfoDTO();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        ResponseEntity<?> response = userController.getUserById(1L);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(dto, response.getBody());
    }

    @Test
    void testGetUserById_notFound() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.getUserById(42L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---------- updateUser ----------
    @Test
    void testUpdateUser_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("old");
        user.setRole(Role.USER);

        UserInfoDTO updatedDto = new UserInfoDTO();

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(updatedDto);

        ResponseEntity<?> response = userController.updateUser(1L, updatedDto);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(userMapper).updateEntityFromDTO(updatedDto, user);
        verify(userRepository).save(user);
    }

    @Test
    void testUpdateUser_notFound() {
        UserInfoDTO updatedDto = new UserInfoDTO();
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.updateUser(999L, updatedDto);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    // ---------- deleteUser ----------
    @Test
    void testDeleteUser_success() {
        User user = new User();
        user.setId(1L);
        user.setUsername("deleteMe");
        user.setRole(Role.USER);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ResponseEntity<?> response = userController.deleteUser(1L);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(userRepository).delete(user);
    }

    @Test
    void testDeleteUser_notFound() {
        when(userRepository.findById(404L)).thenReturn(Optional.empty());

        ResponseEntity<?> response = userController.deleteUser(404L);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }
}
