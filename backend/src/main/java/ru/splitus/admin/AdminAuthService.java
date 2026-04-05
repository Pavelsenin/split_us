package ru.splitus.admin;

import java.util.Optional;
import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

/**
 * Coordinates admin authentication operations.
 */
@Service
public class AdminAuthService {

    static final String ADMIN_LOGIN_SESSION_KEY = "splitus.admin.login";

    private final AdminUserRepository adminUserRepository;
    private final AdminPasswordHasher adminPasswordHasher;

    /**
     * Creates a new admin auth service instance.
     */
    public AdminAuthService(AdminUserRepository adminUserRepository, AdminPasswordHasher adminPasswordHasher) {
        this.adminUserRepository = adminUserRepository;
        this.adminPasswordHasher = adminPasswordHasher;
    }

    /**
     * Authenticates admin user.
     */
    public Optional<AdminUser> authenticate(String login, String password) {
        String normalizedLogin = normalize(login);
        if (normalizedLogin == null || password == null || password.trim().isEmpty()) {
            return Optional.empty();
        }
        Optional<AdminUser> adminUser = adminUserRepository.findByLogin(normalizedLogin);
        if (!adminUser.isPresent()) {
            return Optional.empty();
        }
        return adminPasswordHasher.matches(password, adminUser.get().getPasswordHash()) ? adminUser : Optional.<AdminUser>empty();
    }

    /**
     * Stores authenticated admin login in the session.
     */
    public void signIn(HttpSession session, String login) {
        session.setAttribute(ADMIN_LOGIN_SESSION_KEY, login);
    }

    /**
     * Clears admin session state.
     */
    public void signOut(HttpSession session) {
        if (session != null) {
            session.invalidate();
        }
    }

    /**
     * Returns the current authenticated admin login.
     */
    public String currentLogin(HttpSession session) {
        if (session == null) {
            return null;
        }
        try {
            Object value = session.getAttribute(ADMIN_LOGIN_SESSION_KEY);
            return value instanceof String ? (String) value : null;
        } catch (IllegalStateException exception) {
            return null;
        }
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
