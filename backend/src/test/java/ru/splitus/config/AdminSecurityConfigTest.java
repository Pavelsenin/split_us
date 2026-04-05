package ru.splitus.config;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.RequestRejectedException;

/**
 * Tests admin security configuration.
 */
class AdminSecurityConfigTest {

    @Test
    void customFirewallAllowsSemicolonInUrl() {
        AdminSecurityConfig configuration = new AdminSecurityConfig();
        HttpFirewall firewall = configuration.httpFirewall();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/start;deep_link=join_token");

        Assertions.assertDoesNotThrow(() -> firewall.getFirewalledRequest(request));
    }

    @Test
    void defaultStrictFirewallWouldRejectSameUrl() {
        org.springframework.security.web.firewall.StrictHttpFirewall firewall =
                new org.springframework.security.web.firewall.StrictHttpFirewall();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/start;deep_link=join_token");

        Assertions.assertThrows(RequestRejectedException.class, () -> firewall.getFirewalledRequest(request));
    }
}
