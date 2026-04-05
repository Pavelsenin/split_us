package ru.splitus.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Guards admin routes with session-based authentication.
 */
@Component
public class AdminSessionInterceptor implements HandlerInterceptor {

    private final AdminAuthService adminAuthService;

    /**
     * Creates a new admin session interceptor instance.
     */
    public AdminSessionInterceptor(AdminAuthService adminAuthService) {
        this.adminAuthService = adminAuthService;
    }

    /**
     * Checks whether the current request is authenticated for admin routes.
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        HttpSession session = request.getSession(false);
        if (adminAuthService.currentLogin(session) != null) {
            return true;
        }
        response.sendRedirect("/admin/login");
        return false;
    }
}
