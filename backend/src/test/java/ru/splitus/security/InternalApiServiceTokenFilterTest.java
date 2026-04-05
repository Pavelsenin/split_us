package ru.splitus.security;

import javax.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import ru.splitus.config.InternalApiSecurityProperties;

/**
 * Tests internal api service token filter.
 */
class InternalApiServiceTokenFilterTest {

    @Test
    void rejectsMissingTokenForProtectedPath() throws ServletException, IOException {
        InternalApiServiceTokenFilter filter = new InternalApiServiceTokenFilter(properties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/checks");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Assertions.assertEquals(401, response.getStatus());
        Assertions.assertTrue(response.getContentAsString().contains("AUTHENTICATION_FAILED"));
    }

    @Test
    void skipsHealthEndpoint() throws ServletException, IOException {
        InternalApiServiceTokenFilter filter = new InternalApiServiceTokenFilter(properties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/internal/health/live");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();

        filter.doFilter(request, response, chain);

        Assertions.assertEquals(200, response.getStatus());
    }

    @Test
    void acceptsMatchingToken() throws ServletException, IOException {
        InternalApiServiceTokenFilter filter = new InternalApiServiceTokenFilter(properties("secret"));
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/internal/checks");
        request.addHeader("X-Service-Token", "secret");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, new MockFilterChain());

        Assertions.assertEquals(200, response.getStatus());
    }

    private InternalApiSecurityProperties properties(String token) {
        InternalApiSecurityProperties properties = new InternalApiSecurityProperties();
        properties.setServiceToken(token);
        return properties;
    }
}



