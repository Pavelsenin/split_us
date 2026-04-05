package ru.splitus.check.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.splitus.check.AppUser;
import ru.splitus.check.AppUserRepository;

/**
 * Represents jdbc app user repository.
 */
@Repository
public class JdbcAppUserRepository implements AppUserRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new jdbc app user repository instance.
     */
    public JdbcAppUserRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Finds by telegram user id.
     */
    @Override
    public Optional<AppUser> findByTelegramUserId(long telegramUserId) {
        MapSqlParameterSource parameters = new MapSqlParameterSource("telegramUserId", Long.valueOf(telegramUserId));
        return jdbcTemplate.query(
                "select id, telegram_user_id, telegram_username, registered_at, updated_at "
                        + "from app_user where telegram_user_id = :telegramUserId",
                parameters,
                APP_USER_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Executes save.
     */
    @Override
    public AppUser save(AppUser user) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("telegramUserId", Long.valueOf(user.getTelegramUserId()))
                .addValue("telegramUsername", user.getTelegramUsername())
                .addValue("registeredAt", user.getRegisteredAt())
                .addValue("updatedAt", user.getUpdatedAt());
        jdbcTemplate.update(
                "insert into app_user(id, telegram_user_id, telegram_username, registered_at, updated_at) "
                        + "values (:id, :telegramUserId, :telegramUsername, :registeredAt, :updatedAt)",
                parameters
        );
        return user;
    }

    /**
     * Updates username.
     */
    @Override
    public AppUser updateUsername(AppUser user, String telegramUsername) {
        OffsetDateTime updatedAt = OffsetDateTime.now();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("telegramUsername", telegramUsername)
                .addValue("updatedAt", updatedAt);
        jdbcTemplate.update(
                "update app_user set telegram_username = :telegramUsername, updated_at = :updatedAt where id = :id",
                parameters
        );
        return new AppUser(user.getId(), user.getTelegramUserId(), telegramUsername, user.getRegisteredAt(), updatedAt);
    }

    private static final RowMapper<AppUser> APP_USER_ROW_MAPPER = new RowMapper<AppUser>() {
        @Override
        public AppUser mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AppUser(
                    rs.getObject("id", UUID.class),
                    rs.getLong("telegram_user_id"),
                    rs.getString("telegram_username"),
                    rs.getObject("registered_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };
}




