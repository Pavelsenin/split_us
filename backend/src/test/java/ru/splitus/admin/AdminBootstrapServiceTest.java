package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.splitus.config.AdminSecurityProperties;

/**
 * Tests admin bootstrap service.
 */
class AdminBootstrapServiceTest {

    @Test
    void createsBootstrapAdminFromPlainPassword() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        AdminSecurityProperties properties = new AdminSecurityProperties();
        properties.setBootstrapLogin("admin");
        properties.setBootstrapPassword("secret");
        PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        new AdminBootstrapService(repository, properties, passwordEncoder).bootstrapAdmin();

        AdminUser adminUser = repository.findByLogin("admin").orElseThrow(AssertionError::new);
        Assertions.assertTrue(passwordEncoder.matches("secret", adminUser.getPasswordHash()));
    }

    @Test
    void failsWhenBothPasswordAndHashAreConfigured() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        AdminSecurityProperties properties = new AdminSecurityProperties();
        properties.setBootstrapLogin("admin");
        properties.setBootstrapPassword("secret");
        properties.setBootstrapPasswordHash("$2a$10$hash");

        IllegalStateException exception = Assertions.assertThrows(
                IllegalStateException.class,
                () -> new AdminBootstrapService(repository, properties, new BCryptPasswordEncoder()).bootstrapAdmin()
        );

        Assertions.assertTrue(exception.getMessage().contains("Only one of"));
    }

    /**
     * In-memory admin user repository.
     */
    private static class InMemoryAdminUserRepository implements AdminUserRepository {
        private final Map<String, AdminUser> usersByLogin = new LinkedHashMap<String, AdminUser>();

        /**
         * Finds an admin user by login.
         */
        @Override
        public Optional<AdminUser> findByLogin(String login) {
            return Optional.ofNullable(usersByLogin.get(login));
        }

        /**
         * Saves a new admin user.
         */
        @Override
        public AdminUser save(AdminUser adminUser) {
            usersByLogin.put(adminUser.getLogin(), adminUser);
            return adminUser;
        }

        /**
         * Updates the password hash for an existing admin user.
         */
        @Override
        public AdminUser updatePasswordHash(AdminUser adminUser, String passwordHash) {
            AdminUser updated = new AdminUser(
                    adminUser.getId(),
                    adminUser.getLogin(),
                    passwordHash,
                    adminUser.getCreatedAt(),
                    OffsetDateTime.now()
            );
            usersByLogin.put(updated.getLogin(), updated);
            return updated;
        }
    }
}
