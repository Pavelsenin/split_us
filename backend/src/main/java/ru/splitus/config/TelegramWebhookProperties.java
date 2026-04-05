package ru.splitus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds telegram webhook configuration properties.
 */
@ConfigurationProperties(prefix = "splitus.telegram.webhook")
public class TelegramWebhookProperties {

    private String pathAlias;
    private String secretToken;
    private String botUsername;

    /**
     * Returns the path alias.
     */
    public String getPathAlias() {
        return pathAlias;
    }

    /**
     * Updates the path alias.
     */
    public void setPathAlias(String pathAlias) {
        this.pathAlias = pathAlias;
    }

    /**
     * Returns the secret token.
     */
    public String getSecretToken() {
        return secretToken;
    }

    /**
     * Updates the secret token.
     */
    public void setSecretToken(String secretToken) {
        this.secretToken = secretToken;
    }

    /**
     * Returns the bot username.
     */
    public String getBotUsername() {
        return botUsername;
    }

    /**
     * Updates the bot username.
     */
    public void setBotUsername(String botUsername) {
        this.botUsername = botUsername;
    }
}



