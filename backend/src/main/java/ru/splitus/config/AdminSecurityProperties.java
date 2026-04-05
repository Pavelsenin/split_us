package ru.splitus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds admin security configuration properties.
 */
@ConfigurationProperties(prefix = "splitus.admin")
public class AdminSecurityProperties {

    private String bootstrapLogin;
    private String bootstrapPasswordHash;
    private String environmentName = "local";

    /**
     * Returns the bootstrap login.
     */
    public String getBootstrapLogin() {
        return bootstrapLogin;
    }

    /**
     * Updates the bootstrap login.
     */
    public void setBootstrapLogin(String bootstrapLogin) {
        this.bootstrapLogin = bootstrapLogin;
    }

    /**
     * Returns the bootstrap password hash.
     */
    public String getBootstrapPasswordHash() {
        return bootstrapPasswordHash;
    }

    /**
     * Updates the bootstrap password hash.
     */
    public void setBootstrapPasswordHash(String bootstrapPasswordHash) {
        this.bootstrapPasswordHash = bootstrapPasswordHash;
    }

    /**
     * Returns the environment name.
     */
    public String getEnvironmentName() {
        return environmentName;
    }

    /**
     * Updates the environment name.
     */
    public void setEnvironmentName(String environmentName) {
        this.environmentName = environmentName;
    }
}
