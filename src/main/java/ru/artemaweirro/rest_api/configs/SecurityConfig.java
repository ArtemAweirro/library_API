package ru.artemaweirro.rest_api.configs;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.artemaweirro.rest_api.exception.CustomAccessDeniedHandler;
import ru.artemaweirro.rest_api.exception.CustomAuthenticationEntryPoint;
import ru.artemaweirro.rest_api.repositories.UserRepository;
import ru.artemaweirro.rest_api.security.JwtAuthenticationFilter;
import ru.artemaweirro.rest_api.security.JwtTokenProvider;

@Configuration
public class SecurityConfig {

    private final JwtTokenProvider jwtTokenProvider;
    private final CustomAccessDeniedHandler customAccessDeniedHandler;
    private final CustomAuthenticationEntryPoint customAuthenticationEntryPoint;
    private final UserRepository userRepository;

    public SecurityConfig(JwtTokenProvider jwtTokenProvider,
                          CustomAccessDeniedHandler customAccessDeniedHandler,
                          CustomAuthenticationEntryPoint customAuthenticationEntryPoint,
                          UserRepository userRepository) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.customAccessDeniedHandler = customAccessDeniedHandler;
        this.customAuthenticationEntryPoint = customAuthenticationEntryPoint;
        this.userRepository = userRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        JwtAuthenticationFilter jwtAuthenticationFilter =
                new JwtAuthenticationFilter(jwtTokenProvider, userRepository);

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login/", "/api/auth/register/").permitAll()  // Для регистрации и логина
                        .requestMatchers("/api/orders/**", "api/users/me/" ).authenticated() // Заказы могут смотреть авторизованные
                        .requestMatchers(HttpMethod.GET, "/api/books/**").permitAll() // Все могут посмотреть книги
                        .requestMatchers(HttpMethod.POST, "/api/books/**").hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/books/**").hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/books/**").hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/books/**").hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers(  "/api/users/**").hasRole("ADMIN")
                        .anyRequest().permitAll()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(exceptionHandling -> exceptionHandling
                        .authenticationEntryPoint(customAuthenticationEntryPoint)
                        .accessDeniedHandler(customAccessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}