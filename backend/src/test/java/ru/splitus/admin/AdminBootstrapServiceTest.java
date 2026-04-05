package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import ru.splitus.config.AdminSecurityProperties;

/**
 * Tests admin bootstrap service.
 */
class AdminBootstrapServiceTest {

    @Test
    void createsBootstrapAdminWhenPropertiesAreProvided() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        AdminSecurityProperties properties = new AdminSecurityProperties();
        properties.setBootstrapLogin("admin");
        properties.setBootstrapPasswordHash("$2a$10$hash");
        AdminBootstrapService service = new AdminBootstrapService(repository, properties);

        service.bootstrapAdminUser();

        Assertions.assertEquals("$2a$10$hash", repository.users.get("admin").getPasswordHash());
    }

    @Test
    void skipsBootstrapWhenPropertiesAreMissing() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        AdminBootstrapService service = new AdminBootstrapService(repository, new AdminSecurityProperties());

        service.bootstrapAdminUser();

        Assertions.assertTrue(repository.users.isEmpty());
    }

    @Test
    void rejectsPartialBootstrapConfiguration() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        AdminSecurityProperties properties = new AdminSecurityProperties();
        properties.setBootstrapLogin("admin");
        AdminBootstrapService service = new AdminBootstrapService(repository, properties);

        Assertions.assertThrows(IllegalStateException.class, service::bootstrapAdminUser);
    }

    /**
     * Represents in memory admin user repository.
     */
    private static class InMemoryAdminUserRepository implements AdminUserRepository {
        private final Map<String, AdminUser> users = new HashMap<String, AdminUser>();

        /**
         * Finds by login.
         */
        @Override
        public Optional<AdminUser> findByLogin(String login) {
            return Optional.ofNullable(users.get(login));
        }

        /**
         * Updates bootstrap user.
         */
        @Override
        public void upsertBootstrapUser(String login, String passwordHash) {
            users.put(login, new AdminUser(UUID.randomUUID(), login, passwordHash, OffsetDateTime.now(), OffsetDateTime.now()));
        }
    }
}
