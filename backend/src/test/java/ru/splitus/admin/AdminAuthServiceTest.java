package ru.splitus.admin;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpSession;

/**
 * Tests admin auth service.
 */
class AdminAuthServiceTest {

    @Test
    void authenticatesExistingAdminUser() {
        AdminPasswordHasher passwordHasher = new AdminPasswordHasher();
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        repository.users.put("admin", new AdminUser(
                UUID.randomUUID(),
                "admin",
                passwordHasher.hashPassword("secret"),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        ));
        AdminAuthService service = new AdminAuthService(repository, passwordHasher);

        Optional<AdminUser> adminUser = service.authenticate(" admin ", "secret");

        Assertions.assertTrue(adminUser.isPresent());
        Assertions.assertEquals("admin", adminUser.get().getLogin());
    }

    @Test
    void rejectsWrongPassword() {
        AdminPasswordHasher passwordHasher = new AdminPasswordHasher();
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        repository.users.put("admin", new AdminUser(
                UUID.randomUUID(),
                "admin",
                passwordHasher.hashPassword("secret"),
                OffsetDateTime.now(),
                OffsetDateTime.now()
        ));
        AdminAuthService service = new AdminAuthService(repository, passwordHasher);

        Assertions.assertFalse(service.authenticate("admin", "wrong").isPresent());
    }

    @Test
    void storesAndClearsSessionLogin() {
        AdminAuthService service = new AdminAuthService(new InMemoryAdminUserRepository(), new AdminPasswordHasher());
        MockHttpSession session = new MockHttpSession();

        service.signIn(session, "admin");
        Assertions.assertEquals("admin", service.currentLogin(session));

        service.signOut(session);
        Assertions.assertNull(service.currentLogin(session));
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
