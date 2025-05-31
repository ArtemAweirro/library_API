package ru.artemaweirro.rest_api.models;

import jakarta.persistence.*;
import lombok.*; // позволяет упростить код (автоматические get, set и др)
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

@Getter // автоматический метод get()
@Setter // автоматический метод set()
@NoArgsConstructor // автоматически создает пустой конструктор
@AllArgsConstructor // автоматически создает конструктор со всеми полями
@Entity // говорит о том, что это сущность в БД
@Table(name = "users") // имя таблицы в БД
public class User implements UserDetails {
    @Id // первичный ключ
    @GeneratedValue(strategy = GenerationType.IDENTITY) // автоинкремент
    private Long id;

    @Column(unique = true, nullable = false) // поле username
    private String username;

    @Column(nullable = false) // поле password
    private String password;

    @Enumerated(EnumType.STRING) // сохраняет роль как строку
    private Role role; // Enum с ролями пользователей

    @OneToMany(mappedBy = "user", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Order> orders;

    // Реализация методов UserDetails:

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + this.role.name()));
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
