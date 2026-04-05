package ru.splitus.security;

import java.io.IOException;
import java.time.OffsetDateTime;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import ru.splitus.config.InternalApiSecurityProperties;

/**
 * Filters internal api service token requests.
 */
@Component
public class InternalApiServiceTokenFilter extends OncePerRequestFilter {

    private static final String SERVICE_TOKEN_HEADER = "X-Service-Token";

    private final InternalApiSecurityProperties properties;

    /**
     * Creates a new internal api service token filter instance.
     */
    public InternalApiServiceTokenFilter(InternalApiSecurityProperties properties) {
        this.properties = properties;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.startsWith("/api/internal/")
                || path.startsWith("/api/internal/health/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        String configuredToken = properties.getServiceToken();
        String providedToken = request.getHeader(SERVICE_TOKEN_HEADER);

        if (configuredToken != null && configuredToken.equals(providedToken)) {
            filterChain.doFilter(request, response);
            return;
        }

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(
                "{\"code\":\"AUTHENTICATION_FAILED\",\"message\":\"Invalid service token\",\"timestamp\":\""
                        + OffsetDateTime.now() + "\"}"
        );
    }
}




