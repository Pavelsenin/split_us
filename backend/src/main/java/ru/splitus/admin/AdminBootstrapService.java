package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import javax.annotation.PostConstruct;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.splitus.config.AdminSecurityProperties;

/**
 * Bootstraps a single admin user from configuration for environments without a dedicated provisioning flow.
 */
@Service
public class AdminBootstrapService {

    private final AdminUserRepository adminUserRepository;
    private final AdminSecurityProperties properties;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a new admin bootstrap service instance.
     */
    public AdminBootstrapService(
            AdminUserRepository adminUserRepository,
            AdminSecurityProperties properties,
            PasswordEncoder passwordEncoder) {
        this.adminUserRepository = adminUserRepository;
        this.properties = properties;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Bootstraps the configured admin user.
     */
    @PostConstruct
    public void bootstrapAdmin() {
        boolean hasLogin = hasText(properties.getBootstrapLogin());
        boolean hasPassword = hasText(properties.getBootstrapPassword());
        boolean hasPasswordHash = hasText(properties.getBootstrapPasswordHash());

        if (hasPassword && hasPasswordHash) {
            throw new IllegalStateException("Only one of splitus.admin.bootstrap-password or splitus.admin.bootstrap-password-hash can be configured");
        }
        if (hasLogin != (hasPassword || hasPasswordHash)) {
            throw new IllegalStateException("splitus.admin.bootstrap-login must be configured together with a password or password hash");
        }
        if (!hasLogin) {
            return;
        }

        String login = properties.getBootstrapLogin().trim();
        String passwordHash = hasPasswordHash
                ? properties.getBootstrapPasswordHash().trim()
                : passwordEncoder.encode(properties.getBootstrapPassword());

        Optional<AdminUser> existing = adminUserRepository.findByLogin(login);
        if (existing.isPresent()) {
            if (!passwordHash.equals(existing.get().getPasswordHash())) {
                adminUserRepository.updatePasswordHash(existing.get(), passwordHash);
            }
            return;
        }

        OffsetDateTime now = OffsetDateTime.now();
        adminUserRepository.save(new AdminUser(UUID.randomUUID(), login, passwordHash, now, now));
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
