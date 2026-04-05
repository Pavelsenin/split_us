package ru.splitus.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Holds internal api security configuration properties.
 */
@ConfigurationProperties(prefix = "splitus.security")
public class InternalApiSecurityProperties {

    private String serviceToken;

    /**
     * Returns the service token.
     */
    public String getServiceToken() {
        return serviceToken;
    }

    /**
     * Updates the service token.
     */
    public void setServiceToken(String serviceToken) {
        this.serviceToken = serviceToken;
    }
}




