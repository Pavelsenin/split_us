package ru.splitus.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import ru.splitus.check.ParticipantType;
import ru.splitus.expense.ExpenseStatus;

/**
 * Coordinates admin read operations.
 */
@Service
public class AdminReadService {

    private static final int DEFAULT_SEARCH_LIMIT = 50;

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new admin read service instance.
     */
    public AdminReadService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Searches checks for the admin dashboard.
     */
    public List<AdminCheckSummary> searchChecks(String query) {
        String normalizedQuery = normalizeQuery(query);
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("query", normalizedQuery)
                .addValue("likeQuery", normalizedQuery == null ? null : "%" + normalizedQuery.toLowerCase(Locale.ROOT) + "%")
                .addValue("limit", Integer.valueOf(DEFAULT_SEARCH_LIMIT));
        return jdbcTemplate.query(
                "select cb.id, cb.title, owner.telegram_username as owner_telegram_username, cb.invite_token, cb.telegram_chat_id, "
                        + "cb.currency_code, cb.chat_active, cb.created_at, "
                        + "(select count(*) from participant p where p.check_id = cb.id and p.merged_into_participant_id is null) as active_participant_count, "
                        + "(select count(*) from expense e where e.check_id = cb.id) as expense_count "
                        + "from check_book cb "
                        + "join app_user owner on owner.id = cb.owner_user_id "
                        + "where (:query is null "
                        + "   or lower(cb.title) like :likeQuery "
                        + "   or cast(cb.id as text) = :query "
                        + "   or cb.invite_token = :query "
                        + "   or lower(coalesce(owner.telegram_username, '')) like :likeQuery) "
                        + "order by cb.created_at desc "
                        + "limit :limit",
                parameters,
                ADMIN_CHECK_SUMMARY_ROW_MAPPER
        );
    }

    /**
     * Finds check summary.
     */
    public Optional<AdminCheckSummary> findCheckSummary(UUID checkId) {
        return jdbcTemplate.query(
                "select cb.id, cb.title, owner.telegram_username as owner_telegram_username, cb.invite_token, cb.telegram_chat_id, "
                        + "cb.currency_code, cb.chat_active, cb.created_at, "
                        + "(select count(*) from participant p where p.check_id = cb.id and p.merged_into_participant_id is null) as active_participant_count, "
                        + "(select count(*) from expense e where e.check_id = cb.id) as expense_count "
                        + "from check_book cb "
                        + "join app_user owner on owner.id = cb.owner_user_id "
                        + "where cb.id = :checkId",
                new MapSqlParameterSource("checkId", checkId),
                ADMIN_CHECK_SUMMARY_ROW_MAPPER
        ).stream().findFirst();
    }

    /**
     * Finds check details.
     */
    public Optional<AdminCheckDetails> findCheckDetails(UUID checkId) {
        Optional<AdminCheckSummary> summary = findCheckSummary(checkId);
        if (!summary.isPresent()) {
            return Optional.empty();
        }
        List<AdminParticipantView> participants = loadParticipants(checkId);
        List<AdminParticipantMergeView> merges = loadMerges(checkId);
        Map<UUID, List<AdminExpenseShareView>> sharesByExpenseId = loadShares(checkId);
        List<AdminExpenseView> expenses = loadExpenses(checkId, sharesByExpenseId);
        return Optional.of(new AdminCheckDetails(summary.get(), participants, merges, expenses));
    }

    private List<AdminParticipantView> loadParticipants(UUID checkId) {
        return jdbcTemplate.query(
                "select p.id, p.participant_type, p.display_name, app.telegram_username as linked_telegram_username, "
                        + "p.merged_into_participant_id, p.created_at "
                        + "from participant p "
                        + "left join app_user app on app.id = p.linked_user_id "
                        + "where p.check_id = :checkId "
                        + "order by p.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                ADMIN_PARTICIPANT_VIEW_ROW_MAPPER
        );
    }

    private List<AdminParticipantMergeView> loadMerges(UUID checkId) {
        return jdbcTemplate.query(
                "select pm.id, source.display_name as source_display_name, target.display_name as target_display_name, "
                        + "actor.display_name as performed_by_display_name, pm.created_at "
                        + "from participant_merge pm "
                        + "join participant source on source.id = pm.source_participant_id "
                        + "join participant target on target.id = pm.target_participant_id "
                        + "join participant actor on actor.id = pm.performed_by_participant_id "
                        + "where pm.check_id = :checkId "
                        + "order by pm.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                ADMIN_PARTICIPANT_MERGE_VIEW_ROW_MAPPER
        );
    }

    private Map<UUID, List<AdminExpenseShareView>> loadShares(UUID checkId) {
        List<AdminExpenseShareRow> rows = jdbcTemplate.query(
                "select e.id as expense_id, p.id as participant_id, p.display_name as participant_display_name, es.share_minor "
                        + "from expense e "
                        + "join expense_share es on es.expense_id = e.id "
                        + "join participant p on p.id = es.participant_id "
                        + "where e.check_id = :checkId "
                        + "order by e.created_at asc, p.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                ADMIN_EXPENSE_SHARE_ROW_MAPPER
        );
        Map<UUID, List<AdminExpenseShareView>> result = new LinkedHashMap<UUID, List<AdminExpenseShareView>>();
        for (AdminExpenseShareRow row : rows) {
            List<AdminExpenseShareView> shares = result.get(row.expenseId);
            if (shares == null) {
                shares = new ArrayList<AdminExpenseShareView>();
                result.put(row.expenseId, shares);
            }
            shares.add(new AdminExpenseShareView(row.participantId, row.participantDisplayName, row.shareMinor));
        }
        return result;
    }

    private List<AdminExpenseView> loadExpenses(UUID checkId, Map<UUID, List<AdminExpenseShareView>> sharesByExpenseId) {
        List<AdminExpenseRow> rows = jdbcTemplate.query(
                "select e.id, e.amount_minor, e.currency_code, e.status, payer.display_name as payer_display_name, e.comment, "
                        + "e.source_message_text, e.telegram_chat_id, e.telegram_message_id, creator.display_name as created_by_display_name, "
                        + "updater.display_name as updated_by_display_name, e.created_at, e.updated_at "
                        + "from expense e "
                        + "join participant payer on payer.id = e.payer_participant_id "
                        + "join participant creator on creator.id = e.created_by_participant_id "
                        + "join participant updater on updater.id = e.updated_by_participant_id "
                        + "where e.check_id = :checkId "
                        + "order by e.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                ADMIN_EXPENSE_ROW_MAPPER
        );

        List<AdminExpenseView> expenses = new ArrayList<AdminExpenseView>();
        for (AdminExpenseRow row : rows) {
            List<AdminExpenseShareView> shares = sharesByExpenseId.get(row.id);
            expenses.add(new AdminExpenseView(
                    row.id,
                    row.amountMinor,
                    row.currencyCode,
                    row.status,
                    row.payerDisplayName,
                    row.comment,
                    row.sourceMessageText,
                    row.telegramChatId,
                    row.telegramMessageId,
                    row.createdByDisplayName,
                    row.updatedByDisplayName,
                    row.createdAt,
                    row.updatedAt,
                    shares == null ? java.util.Collections.<AdminExpenseShareView>emptyList() : shares
            ));
        }
        return expenses;
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        String trimmed = query.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private static final RowMapper<AdminCheckSummary> ADMIN_CHECK_SUMMARY_ROW_MAPPER = new RowMapper<AdminCheckSummary>() {
        @Override
        public AdminCheckSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long telegramChatId = rs.getObject("telegram_chat_id") == null ? null : Long.valueOf(rs.getLong("telegram_chat_id"));
            return new AdminCheckSummary(
                    rs.getObject("id", UUID.class),
                    rs.getString("title"),
                    rs.getString("owner_telegram_username"),
                    rs.getString("invite_token"),
                    telegramChatId,
                    rs.getString("currency_code"),
                    rs.getBoolean("chat_active"),
                    rs.getInt("active_participant_count"),
                    rs.getInt("expense_count"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminParticipantView> ADMIN_PARTICIPANT_VIEW_ROW_MAPPER = new RowMapper<AdminParticipantView>() {
        @Override
        public AdminParticipantView mapRow(ResultSet rs, int rowNum) throws SQLException {
            UUID mergedIntoParticipantId = rs.getObject("merged_into_participant_id", UUID.class);
            return new AdminParticipantView(
                    rs.getObject("id", UUID.class),
                    ParticipantType.valueOf(rs.getString("participant_type")),
                    rs.getString("display_name"),
                    rs.getString("linked_telegram_username"),
                    mergedIntoParticipantId,
                    mergedIntoParticipantId == null,
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminParticipantMergeView> ADMIN_PARTICIPANT_MERGE_VIEW_ROW_MAPPER =
            new RowMapper<AdminParticipantMergeView>() {
                @Override
                public AdminParticipantMergeView mapRow(ResultSet rs, int rowNum) throws SQLException {
                    return new AdminParticipantMergeView(
                            rs.getObject("id", UUID.class),
                            rs.getString("source_display_name"),
                            rs.getString("target_display_name"),
                            rs.getString("performed_by_display_name"),
                            rs.getObject("created_at", OffsetDateTime.class)
                    );
                }
            };

    private static final RowMapper<AdminExpenseRow> ADMIN_EXPENSE_ROW_MAPPER = new RowMapper<AdminExpenseRow>() {
        @Override
        public AdminExpenseRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long telegramChatId = rs.getObject("telegram_chat_id") == null ? null : Long.valueOf(rs.getLong("telegram_chat_id"));
            Long telegramMessageId = rs.getObject("telegram_message_id") == null ? null : Long.valueOf(rs.getLong("telegram_message_id"));
            return new AdminExpenseRow(
                    rs.getObject("id", UUID.class),
                    rs.getLong("amount_minor"),
                    rs.getString("currency_code"),
                    ExpenseStatus.valueOf(rs.getString("status")),
                    rs.getString("payer_display_name"),
                    rs.getString("comment"),
                    rs.getString("source_message_text"),
                    telegramChatId,
                    telegramMessageId,
                    rs.getString("created_by_display_name"),
                    rs.getString("updated_by_display_name"),
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminExpenseShareRow> ADMIN_EXPENSE_SHARE_ROW_MAPPER = new RowMapper<AdminExpenseShareRow>() {
        @Override
        public AdminExpenseShareRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminExpenseShareRow(
                    rs.getObject("expense_id", UUID.class),
                    rs.getObject("participant_id", UUID.class),
                    rs.getString("participant_display_name"),
                    rs.getLong("share_minor")
            );
        }
    };

    /**
     * Represents admin expense row.
     */
    private static class AdminExpenseRow {
        private final UUID id;
        private final long amountMinor;
        private final String currencyCode;
        private final ExpenseStatus status;
        private final String payerDisplayName;
        private final String comment;
        private final String sourceMessageText;
        private final Long telegramChatId;
        private final Long telegramMessageId;
        private final String createdByDisplayName;
        private final String updatedByDisplayName;
        private final OffsetDateTime createdAt;
        private final OffsetDateTime updatedAt;

        private AdminExpenseRow(
                UUID id,
                long amountMinor,
                String currencyCode,
                ExpenseStatus status,
                String payerDisplayName,
                String comment,
                String sourceMessageText,
                Long telegramChatId,
                Long telegramMessageId,
                String createdByDisplayName,
                String updatedByDisplayName,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt) {
            this.id = id;
            this.amountMinor = amountMinor;
            this.currencyCode = currencyCode;
            this.status = status;
            this.payerDisplayName = payerDisplayName;
            this.comment = comment;
            this.sourceMessageText = sourceMessageText;
            this.telegramChatId = telegramChatId;
            this.telegramMessageId = telegramMessageId;
            this.createdByDisplayName = createdByDisplayName;
            this.updatedByDisplayName = updatedByDisplayName;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }
    }

    /**
     * Represents admin expense share row.
     */
    private static class AdminExpenseShareRow {
        private final UUID expenseId;
        private final UUID participantId;
        private final String participantDisplayName;
        private final long shareMinor;

        private AdminExpenseShareRow(UUID expenseId, UUID participantId, String participantDisplayName, long shareMinor) {
            this.expenseId = expenseId;
            this.participantId = participantId;
            this.participantDisplayName = participantDisplayName;
            this.shareMinor = shareMinor;
        }
    }
}
