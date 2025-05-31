package ru.artemaweirro.rest_api.initialization;

import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import ru.artemaweirro.rest_api.models.User;
import ru.artemaweirro.rest_api.models.Role;
import ru.artemaweirro.rest_api.repositories.UserRepository;

@Component
public class InitAdmin implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public InitAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.findByUsername("admin").isEmpty()) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin_password")); // обязательно шифруем
            admin.setRole(Role.ADMIN);

            userRepository.save(admin);
            System.out.println("Администратор создан!");
        }
    }
}