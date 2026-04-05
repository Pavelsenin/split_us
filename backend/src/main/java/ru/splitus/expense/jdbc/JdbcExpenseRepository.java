package ru.splitus.expense.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseStatus;

/**
 * Represents jdbc expense repository.
 */
@Repository
public class JdbcExpenseRepository implements ExpenseRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new jdbc expense repository instance.
     */
    public JdbcExpenseRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes save.
     */
    @Override
    public Expense save(Expense expense) {
        jdbcTemplate.update(
                "insert into expense("
                        + "id, check_id, amount_minor, currency_code, payer_participant_id, comment, source_message_text, "
                        + "telegram_chat_id, telegram_message_id, status, created_by_participant_id, updated_by_participant_id, created_at, updated_at"
                        + ") values ("
                        + ":id, :checkId, :amountMinor, :currencyCode, :payerParticipantId, :comment, :sourceMessageText, "
                        + ":telegramChatId, :telegramMessageId, :status, :createdByParticipantId, :updatedByParticipantId, :createdAt, :updatedAt"
                        + ")",
                parametersFor(expense)
        );
        return expense;
    }

    /**
     * Executes update.
     */
    @Override
    public Expense update(Expense expense) {
        jdbcTemplate.update(
                "update expense set amount_minor = :amountMinor, currency_code = :currencyCode, payer_participant_id = :payerParticipantId, "
                        + "comment = :comment, source_message_text = :sourceMessageText, telegram_chat_id = :telegramChatId, "
                        + "telegram_message_id = :telegramMessageId, status = :status, updated_by_participant_id = :updatedByParticipantId, "
                        + "updated_at = :updatedAt where id = :id",
                parametersFor(expense)
        );
        return expense;
    }

    /**
     * Finds by id.
     */
    @Override
    public Optional<Expense> findById(UUID expenseId) {
        return jdbcTemplate.query(
                "select id, check_id, amount_minor, currency_code, payer_participant_id, comment, source_message_text, "
                        + "telegram_chat_id, telegram_message_id, status, created_by_participant_id, updated_by_participant_id, created_at, updated_at "
                        + "from expense where id = :id",
                new MapSqlParameterSource("id", expenseId),
                EXPENSE_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Finds by telegram message.
     */
    @Override
    public Optional<Expense> findByTelegramMessage(long telegramChatId, long telegramMessageId) {
        return jdbcTemplate.query(
                "select id, check_id, amount_minor, currency_code, payer_participant_id, comment, source_message_text, "
                        + "telegram_chat_id, telegram_message_id, status, created_by_participant_id, updated_by_participant_id, created_at, updated_at "
                        + "from expense where telegram_chat_id = :telegramChatId and telegram_message_id = :telegramMessageId",
                new MapSqlParameterSource()
                        .addValue("telegramChatId", Long.valueOf(telegramChatId))
                        .addValue("telegramMessageId", Long.valueOf(telegramMessageId)),
                EXPENSE_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Finds by check id.
     */
    @Override
    public List<Expense> findByCheckId(UUID checkId) {
        return jdbcTemplate.query(
                "select id, check_id, amount_minor, currency_code, payer_participant_id, comment, source_message_text, "
                        + "telegram_chat_id, telegram_message_id, status, created_by_participant_id, updated_by_participant_id, created_at, updated_at "
                        + "from expense where check_id = :checkId order by created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                EXPENSE_ROW_MAPPER
        );
    }

    /**
     * Deletes by id.
     */
    @Override
    public void deleteById(UUID expenseId) {
        jdbcTemplate.update("delete from expense where id = :id", new MapSqlParameterSource("id", expenseId));
    }

    private MapSqlParameterSource parametersFor(Expense expense) {
        return new MapSqlParameterSource()
                .addValue("id", expense.getId())
                .addValue("checkId", expense.getCheckId())
                .addValue("amountMinor", Long.valueOf(expense.getAmountMinor()))
                .addValue("currencyCode", expense.getCurrencyCode())
                .addValue("payerParticipantId", expense.getPayerParticipantId())
                .addValue("comment", expense.getComment())
                .addValue("sourceMessageText", expense.getSourceMessageText())
                .addValue("telegramChatId", expense.getTelegramChatId())
                .addValue("telegramMessageId", expense.getTelegramMessageId())
                .addValue("status", expense.getStatus().name())
                .addValue("createdByParticipantId", expense.getCreatedByParticipantId())
                .addValue("updatedByParticipantId", expense.getUpdatedByParticipantId())
                .addValue("createdAt", expense.getCreatedAt())
                .addValue("updatedAt", expense.getUpdatedAt());
    }

    private static final RowMapper<Expense> EXPENSE_ROW_MAPPER = new RowMapper<Expense>() {
        @Override
        public Expense mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long telegramChatId = rs.getObject("telegram_chat_id") == null ? null : Long.valueOf(rs.getLong("telegram_chat_id"));
            Long telegramMessageId = rs.getObject("telegram_message_id") == null ? null : Long.valueOf(rs.getLong("telegram_message_id"));
            return new Expense(
                    rs.getObject("id", UUID.class),
                    rs.getObject("check_id", UUID.class),
                    rs.getLong("amount_minor"),
                    rs.getString("currency_code"),
                    rs.getObject("payer_participant_id", UUID.class),
                    rs.getString("comment"),
                    rs.getString("source_message_text"),
                    telegramChatId,
                    telegramMessageId,
                    ExpenseStatus.valueOf(rs.getString("status")),
                    rs.getObject("created_by_participant_id", UUID.class),
                    rs.getObject("updated_by_participant_id", UUID.class),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };
}



