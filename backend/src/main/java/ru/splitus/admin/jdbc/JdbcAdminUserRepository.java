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
 * Represents jdbc admin user repository.
 */
@Repository
public class JdbcAdminUserRepository implements AdminUserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new jdbc admin user repository instance.
     */
    public JdbcAdminUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds by login.
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
     * Updates bootstrap user.
     */
    @Override
    public void upsertBootstrapUser(String login, String passwordHash) {
        jdbcTemplate.update(
                "insert into admin_user(login, password_hash) values (:login, :passwordHash) "
                        + "on conflict (login) do update set password_hash = excluded.password_hash, updated_at = now()",
                new MapSqlParameterSource()
                        .addValue("login", login)
                        .addValue("passwordHash", passwordHash)
        );
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
