package ru.splitus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds admin authentication configuration properties.
 */
@ConfigurationProperties(prefix = "splitus.admin")
public class AdminSecurityProperties {

    private String environmentName;
    private String bootstrapLogin;
    private String bootstrapPassword;
    private String bootstrapPasswordHash;

    /**
     * Returns the environment name shown in the admin UI.
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * Updates the environment name shown in the admin UI.
     */
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }

    /**
     * Returns the bootstrap admin login.
     */
    public String getBootstrapLogin() {
        return bootstrapLogin;
    }

    /**
     * Updates the bootstrap admin login.
     */
    public void setBootstrapLogin(String bootstrapLogin) {
        this.bootstrapLogin = bootstrapLogin;
    }

    /**
     * Returns the bootstrap admin password in plain text.
     */
    public String getBootstrapPassword() {
        return bootstrapPassword;
    }

    /**
     * Updates the bootstrap admin password in plain text.
     */
    public void setBootstrapPassword(String bootstrapPassword) {
        this.bootstrapPassword = bootstrapPassword;
    }

    /**
     * Returns the bootstrap admin password hash.
     */
    public String getBootstrapPasswordHash() {
        return bootstrapPasswordHash;
    }

    /**
     * Updates the bootstrap admin password hash.
     */
    public void setBootstrapPasswordHash(String bootstrapPasswordHash) {
        this.bootstrapPasswordHash = bootstrapPasswordHash;
    }
}
