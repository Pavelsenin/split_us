package ru.splitus.security;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import ru.splitus.admin.AdminUser;
import ru.splitus.admin.AdminUserRepository;

/**
 * Tests admin user details service.
 */
class AdminUserDetailsServiceTest {

    @Test
    void loadsAdminUserFromRepository() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();
        repository.save(new AdminUser(
                UUID.randomUUID(),
                "admin",
                "$2a$10$stored-hash",
                OffsetDateTime.parse("2026-04-05T11:00:00+03:00"),
                OffsetDateTime.parse("2026-04-05T11:00:00+03:00")
        ));

        UserDetails userDetails = new AdminUserDetailsService(repository).loadUserByUsername("admin");

        Assertions.assertEquals("admin", userDetails.getUsername());
        Assertions.assertEquals("$2a$10$stored-hash", userDetails.getPassword());
        Assertions.assertFalse(userDetails.getAuthorities().isEmpty());
    }

    @Test
    void throwsWhenAdminUserIsMissing() {
        InMemoryAdminUserRepository repository = new InMemoryAdminUserRepository();

        Assertions.assertThrows(
                UsernameNotFoundException.class,
                () -> new AdminUserDetailsService(repository).loadUserByUsername("missing")
        );
    }

    /**
     * In-memory admin user repository.
     */
    private static class InMemoryAdminUserRepository implements AdminUserRepository {
        private final Map<String, AdminUser> usersByLogin = new HashMap<String, AdminUser>();

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
