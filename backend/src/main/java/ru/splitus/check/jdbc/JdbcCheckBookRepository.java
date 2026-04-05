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
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;

/**
 * Represents jdbc check book repository.
 */
@Repository
public class JdbcCheckBookRepository implements CheckBookRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new jdbc check book repository instance.
     */
    public JdbcCheckBookRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes save.
     */
    @Override
    public CheckBook save(CheckBook checkBook) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", checkBook.getId())
                .addValue("title", checkBook.getTitle())
                .addValue("ownerUserId", checkBook.getOwnerUserId())
                .addValue("inviteToken", checkBook.getInviteToken())
                .addValue("telegramChatId", checkBook.getTelegramChatId())
                .addValue("currencyCode", checkBook.getCurrencyCode())
                .addValue("chatActive", Boolean.valueOf(checkBook.isChatActive()))
                .addValue("createdAt", checkBook.getCreatedAt());
        jdbcTemplate.update(
                "insert into check_book(id, title, owner_user_id, invite_token, telegram_chat_id, currency_code, chat_active, created_at) "
                        + "values (:id, :title, :ownerUserId, :inviteToken, :telegramChatId, :currencyCode, :chatActive, :createdAt)",
                parameters
        );
        return checkBook;
    }

    /**
     * Finds by id.
     */
    @Override
    public Optional<CheckBook> findById(UUID checkId) {
        return jdbcTemplate.query(
                "select id, title, owner_user_id, invite_token, telegram_chat_id, currency_code, chat_active, created_at "
                        + "from check_book where id = :id",
                new MapSqlParameterSource("id", checkId),
                CHECK_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Finds by invite token.
     */
    @Override
    public Optional<CheckBook> findByInviteToken(String inviteToken) {
        return jdbcTemplate.query(
                "select id, title, owner_user_id, invite_token, telegram_chat_id, currency_code, chat_active, created_at "
                        + "from check_book where invite_token = :inviteToken",
                new MapSqlParameterSource("inviteToken", inviteToken),
                CHECK_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Counts created by owner since.
     */
    @Override
    public int countCreatedByOwnerSince(UUID ownerUserId, OffsetDateTime since) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from check_book where owner_user_id = :ownerUserId and created_at >= :since",
                new MapSqlParameterSource().addValue("ownerUserId", ownerUserId).addValue("since", since),
                Integer.class
        );
        return count == null ? 0 : count.intValue();
    }

    private static final RowMapper<CheckBook> CHECK_ROW_MAPPER = new RowMapper<CheckBook>() {
        @Override
        public CheckBook mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long telegramChatId = rs.getObject("telegram_chat_id") == null ? null : Long.valueOf(rs.getLong("telegram_chat_id"));
            return new CheckBook(
                    rs.getObject("id", UUID.class),
                    rs.getString("title"),
                    rs.getObject("owner_user_id", UUID.class),
                    rs.getString("invite_token"),
                    telegramChatId,
                    rs.getString("currency_code"),
                    rs.getBoolean("chat_active"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };
}



