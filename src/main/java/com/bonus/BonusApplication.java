package com.bonus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // Включаем автоматическое заполнение created_at, updated_at
public class BonusApplication {

    public static void main(String[] args) {
        SpringApplication.run(BonusApplication.class, args);
        System.out.println("========================================");
        System.out.println("Бонусный сервис успешно запущен!");
        System.out.println("Swagger UI: http://localhost:8080/swagger-ui.html");
        System.out.println("========================================");
    }
}