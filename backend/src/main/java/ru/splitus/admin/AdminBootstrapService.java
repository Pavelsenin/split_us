package ru.splitus.admin;

import javax.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import ru.splitus.config.AdminSecurityProperties;

/**
 * Coordinates admin bootstrap operations.
 */
@Service
public class AdminBootstrapService {

    private final AdminUserRepository adminUserRepository;
    private final AdminSecurityProperties adminSecurityProperties;

    /**
     * Creates a new admin bootstrap service instance.
     */
    public AdminBootstrapService(AdminUserRepository adminUserRepository, AdminSecurityProperties adminSecurityProperties) {
        this.adminUserRepository = adminUserRepository;
        this.adminSecurityProperties = adminSecurityProperties;
    }

    /**
     * Creates bootstrap admin user from configuration when it is provided.
     */
    @PostConstruct
    public void bootstrapAdminUser() {
        String login = normalize(adminSecurityProperties.getBootstrapLogin());
        String passwordHash = normalize(adminSecurityProperties.getBootstrapPasswordHash());

        if (login == null && passwordHash == null) {
            return;
        }
        if (login == null || passwordHash == null) {
            throw new IllegalStateException("Both splitus.admin.bootstrap-login and splitus.admin.bootstrap-password-hash must be provided");
        }

        adminUserRepository.upsertBootstrapUser(login, passwordHash);
    }

    private String normalize(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
