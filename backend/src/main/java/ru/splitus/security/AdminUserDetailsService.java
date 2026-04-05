package ru.splitus.security;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.splitus.admin.AdminUser;
import ru.splitus.admin.AdminUserRepository;

/**
 * Loads admin users from the database for Spring Security form authentication.
 */
@Service
public class AdminUserDetailsService implements UserDetailsService {

    private final AdminUserRepository adminUserRepository;

    /**
     * Creates a new admin user details service instance.
     */
    public AdminUserDetailsService(AdminUserRepository adminUserRepository) {
        this.adminUserRepository = adminUserRepository;
    }

    /**
     * Loads an admin user by login.
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByLogin(username)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found"));
        return User.withUsername(adminUser.getLogin())
                .password(adminUser.getPasswordHash())
                .roles("ADMIN")
                .build();
    }
}
