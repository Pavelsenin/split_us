package ru.splitus;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.splitus.config.InternalApiSecurityProperties;
import ru.splitus.config.TelegramWebhookProperties;

/**
 * Bootstraps the split us application.
 */
@SpringBootApplication
@EnableConfigurationProperties({TelegramWebhookProperties.class, InternalApiSecurityProperties.class})
public class SplitUsApplication {

    /**
     * Starts the application.
     */
    public static void main(String[] args) {
        SpringApplication.run(SplitUsApplication.class, args);
    }
}



