package ru.splitus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.splitus.config.TelegramWebhookProperties;

@SpringBootApplication
@EnableConfigurationProperties(TelegramWebhookProperties.class)
public class SplitUsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SplitUsApplication.class, args);
    }
}

