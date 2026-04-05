package ru.splitus.admin.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.splitus.admin.AdminUser;
import ru.splitus.admin.AdminUserRepository;

/**
 * JDBC-backed admin user repository.
 */
@Repository
public class JdbcAdminUserRepository implements AdminUserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new JDBC admin user repository instance.
     */
    public JdbcAdminUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds an admin user by login.
     */
    @Override
    public Optional<AdminUser> findByLogin(String login) {
        return jdbcTemplate.query(
                "select id, login, password_hash, created_at, updated_at from admin_user where login = :login",
                new MapSqlParameterSource("login", login),
                ADMIN_USER_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Saves a new admin user.
     */
    @Override
    public AdminUser save(AdminUser adminUser) {
        jdbcTemplate.update(
                "insert into admin_user(id, login, password_hash, created_at, updated_at) "
                        + "values (:id, :login, :passwordHash, :createdAt, :updatedAt)",
                new MapSqlParameterSource()
                        .addValue("id", adminUser.getId())
                        .addValue("login", adminUser.getLogin())
                        .addValue("passwordHash", adminUser.getPasswordHash())
                        .addValue("createdAt", adminUser.getCreatedAt())
                        .addValue("updatedAt", adminUser.getUpdatedAt())
        );
        return adminUser;
    }

    /**
     * Updates the password hash for an existing admin user.
     */
    @Override
    public AdminUser updatePasswordHash(AdminUser adminUser, String passwordHash) {
        OffsetDateTime updatedAt = OffsetDateTime.now();
        jdbcTemplate.update(
                "update admin_user set password_hash = :passwordHash, updated_at = :updatedAt where id = :id",
                new MapSqlParameterSource()
                        .addValue("id", adminUser.getId())
                        .addValue("passwordHash", passwordHash)
                        .addValue("updatedAt", updatedAt)
        );
        return new AdminUser(adminUser.getId(), adminUser.getLogin(), passwordHash, adminUser.getCreatedAt(), updatedAt);
    }

    private static final RowMapper<AdminUser> ADMIN_USER_ROW_MAPPER = new RowMapper<AdminUser>() {
        @Override
        public AdminUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminUser(
                    rs.getObject("id", UUID.class),
                    rs.getString("login"),
                    rs.getString("password_hash"),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };
}
