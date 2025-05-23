package ru.artemaweirro.rest_api.configs;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI apiInfo() {
        return new OpenAPI()
                .info(new Info()
                        .title("Bookstore API")
                        .description("Документация API для онлайн-магазина книг")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Артём Сихварт")
                                .email("sixvart05@bk.ru")));
    }
}
