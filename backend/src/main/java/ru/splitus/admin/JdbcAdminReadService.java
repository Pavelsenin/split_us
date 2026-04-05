package ru.splitus.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splitus.check.ParticipantType;
import ru.splitus.expense.ExpenseStatus;
import ru.splitus.settlement.SettlementQueryService;
import ru.splitus.settlement.SettlementResult;

/**
 * JDBC-backed read-only admin service.
 */
@Service
public class JdbcAdminReadService implements AdminReadService {

    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SettlementQueryService settlementQueryService;

    /**
     * Creates a new JDBC admin read service instance.
     */
    public JdbcAdminReadService(NamedParameterJdbcTemplate jdbcTemplate, SettlementQueryService settlementQueryService) {
        this.jdbcTemplate = jdbcTemplate;
        this.settlementQueryService = settlementQueryService;
    }

    /**
     * Searches checks for the admin dashboard.
     */
    @Override
    @Transactional(readOnly = true)
    public List<AdminCheckSummary> searchChecks(String query) {
        String normalizedQuery = query == null ? "" : query.trim();
        String lowered = normalizedQuery.toLowerCase();
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("query", lowered)
                .addValue("pattern", "%" + lowered + "%");
        return jdbcTemplate.query(
                "select c.id, c.title, c.invite_token, c.currency_code, c.telegram_chat_id, c.chat_active, c.created_at, "
                        + "u.telegram_user_id as owner_telegram_user_id, u.telegram_username as owner_telegram_username, "
                        + "(select count(*) from participant p where p.check_id = c.id and p.merged_into_participant_id is null) as active_participant_count, "
                        + "(select count(*) from expense e where e.check_id = c.id) as expense_count "
                        + "from check_book c "
                        + "join app_user u on u.id = c.owner_user_id "
                        + "where (:query = '' "
                        + "or lower(c.title) like :pattern "
                        + "or lower(c.invite_token) like :pattern "
                        + "or cast(c.id as text) like :pattern "
                        + "or lower(coalesce(u.telegram_username, '')) like :pattern "
                        + "or cast(coalesce(c.telegram_chat_id, 0) as text) like :pattern) "
                        + "order by c.created_at desc "
                        + "limit 20",
                parameters,
                CHECK_SUMMARY_ROW_MAPPER
        );
    }

    /**
     * Loads a full read-only check view for the admin detail page.
     */
    @Override
    @Transactional(readOnly = true)
    public Optional<AdminCheckDetails> findCheckDetails(UUID checkId) {
        Optional<AdminCheckSummary> summary = jdbcTemplate.query(
                "select c.id, c.title, c.invite_token, c.currency_code, c.telegram_chat_id, c.chat_active, c.created_at, "
                        + "u.telegram_user_id as owner_telegram_user_id, u.telegram_username as owner_telegram_username, "
                        + "(select count(*) from participant p where p.check_id = c.id and p.merged_into_participant_id is null) as active_participant_count, "
                        + "(select count(*) from expense e where e.check_id = c.id) as expense_count "
                        + "from check_book c "
                        + "join app_user u on u.id = c.owner_user_id "
                        + "where c.id = :checkId",
                new MapSqlParameterSource("checkId", checkId),
                CHECK_SUMMARY_ROW_MAPPER
        ).stream().findFirst();
        if (!summary.isPresent()) {
            return Optional.empty();
        }

        List<AdminParticipantView> participants = jdbcTemplate.query(
                "select p.id, p.participant_type, p.display_name, p.linked_user_id, linked.telegram_username as linked_telegram_username, "
                        + "p.merged_into_participant_id, merged.display_name as merged_into_display_name, p.created_at "
                        + "from participant p "
                        + "left join app_user linked on linked.id = p.linked_user_id "
                        + "left join participant merged on merged.id = p.merged_into_participant_id "
                        + "where p.check_id = :checkId "
                        + "order by p.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                PARTICIPANT_ROW_MAPPER
        );

        List<AdminParticipantMergeView> merges = jdbcTemplate.query(
                "select pm.id, pm.source_participant_id, source_p.display_name as source_display_name, "
                        + "pm.target_participant_id, target_p.display_name as target_display_name, "
                        + "pm.performed_by_participant_id, performer.display_name as performed_by_display_name, "
                        + "pm.created_at "
                        + "from participant_merge pm "
                        + "join participant source_p on source_p.id = pm.source_participant_id "
                        + "join participant target_p on target_p.id = pm.target_participant_id "
                        + "join participant performer on performer.id = pm.performed_by_participant_id "
                        + "where pm.check_id = :checkId "
                        + "order by pm.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                MERGE_ROW_MAPPER
        );

        List<AdminExpenseRow> expenseRows = jdbcTemplate.query(
                "select e.id, e.amount_minor, e.status, e.comment, e.source_message_text, e.telegram_chat_id, e.telegram_message_id, "
                        + "e.created_at, e.updated_at, payer.display_name as payer_display_name, "
                        + "created_by.display_name as created_by_display_name, updated_by.display_name as updated_by_display_name "
                        + "from expense e "
                        + "join participant payer on payer.id = e.payer_participant_id "
                        + "join participant created_by on created_by.id = e.created_by_participant_id "
                        + "join participant updated_by on updated_by.id = e.updated_by_participant_id "
                        + "where e.check_id = :checkId "
                        + "order by e.created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                EXPENSE_ROW_MAPPER
        );

        Map<UUID, List<AdminExpenseShareView>> sharesByExpenseId = loadSharesByExpenseId(expenseRows);
        List<AdminExpenseView> expenses = new ArrayList<AdminExpenseView>();
        for (AdminExpenseRow expenseRow : expenseRows) {
            List<AdminExpenseShareView> shares = sharesByExpenseId.get(expenseRow.getExpenseId());
            expenses.add(new AdminExpenseView(
                    expenseRow.getExpenseId(),
                    expenseRow.getAmountMinor(),
                    expenseRow.getStatus(),
                    expenseRow.getPayerDisplayName(),
                    expenseRow.getCreatedByDisplayName(),
                    expenseRow.getUpdatedByDisplayName(),
                    expenseRow.getComment(),
                    expenseRow.getSourceMessageText(),
                    expenseRow.getTelegramChatId(),
                    expenseRow.getTelegramMessageId(),
                    expenseRow.getCreatedAt(),
                    expenseRow.getUpdatedAt(),
                    shares == null ? Collections.<AdminExpenseShareView>emptyList() : shares
            ));
        }

        SettlementResult settlement = settlementQueryService.calculate(checkId);
        return Optional.of(new AdminCheckDetails(summary.get(), participants, merges, expenses, settlement));
    }

    private Map<UUID, List<AdminExpenseShareView>> loadSharesByExpenseId(List<AdminExpenseRow> expenseRows) {
        if (expenseRows.isEmpty()) {
            return Collections.emptyMap();
        }
        List<UUID> expenseIds = new ArrayList<UUID>();
        for (AdminExpenseRow row : expenseRows) {
            expenseIds.add(row.getExpenseId());
        }
        List<AdminExpenseShareRow> shareRows = jdbcTemplate.query(
                "select es.expense_id, es.participant_id, p.display_name as participant_display_name, es.share_minor "
                        + "from expense_share es "
                        + "join participant p on p.id = es.participant_id "
                        + "where es.expense_id in (:expenseIds) "
                        + "order by es.expense_id asc, p.display_name asc",
                new MapSqlParameterSource("expenseIds", expenseIds),
                EXPENSE_SHARE_ROW_MAPPER
        );
        Map<UUID, List<AdminExpenseShareView>> result = new LinkedHashMap<UUID, List<AdminExpenseShareView>>();
        for (AdminExpenseShareRow shareRow : shareRows) {
            List<AdminExpenseShareView> shares = result.get(shareRow.getExpenseId());
            if (shares == null) {
                shares = new ArrayList<AdminExpenseShareView>();
                result.put(shareRow.getExpenseId(), shares);
            }
            shares.add(new AdminExpenseShareView(
                    shareRow.getParticipantId(),
                    shareRow.getParticipantDisplayName(),
                    shareRow.getShareMinor()
            ));
        }
        return result;
    }

    private static final RowMapper<AdminCheckSummary> CHECK_SUMMARY_ROW_MAPPER = new RowMapper<AdminCheckSummary>() {
        @Override
        public AdminCheckSummary mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long telegramChatId = rs.getObject("telegram_chat_id") == null ? null : Long.valueOf(rs.getLong("telegram_chat_id"));
            return new AdminCheckSummary(
                    rs.getObject("id", UUID.class),
                    rs.getString("title"),
                    rs.getString("invite_token"),
                    rs.getString("currency_code"),
                    telegramChatId,
                    rs.getBoolean("chat_active"),
                    rs.getLong("owner_telegram_user_id"),
                    rs.getString("owner_telegram_username"),
                    rs.getInt("active_participant_count"),
                    rs.getInt("expense_count"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminParticipantView> PARTICIPANT_ROW_MAPPER = new RowMapper<AdminParticipantView>() {
        @Override
        public AdminParticipantView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminParticipantView(
                    rs.getObject("id", UUID.class),
                    ParticipantType.valueOf(rs.getString("participant_type")),
                    rs.getString("display_name"),
                    rs.getObject("linked_user_id", UUID.class),
                    rs.getString("linked_telegram_username"),
                    rs.getObject("merged_into_participant_id", UUID.class),
                    rs.getString("merged_into_display_name"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminParticipantMergeView> MERGE_ROW_MAPPER = new RowMapper<AdminParticipantMergeView>() {
        @Override
        public AdminParticipantMergeView mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new AdminParticipantMergeView(
                    rs.getObject("id", UUID.class),
                    rs.getObject("source_participant_id", UUID.class),
                    rs.getString("source_display_name"),
                    rs.getObject("target_participant_id", UUID.class),
                    rs.getString("target_display_name"),
                    rs.getObject("performed_by_participant_id", UUID.class),
                    rs.getString("performed_by_display_name"),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminExpenseRow> EXPENSE_ROW_MAPPER = new RowMapper<AdminExpenseRow>() {
        @Override
        public AdminExpenseRow mapRow(ResultSet rs, int rowNum) throws SQLException {
            Long telegramChatId = rs.getObject("telegram_chat_id") == null ? null : Long.valueOf(rs.getLong("telegram_chat_id"));
            Long telegramMessageId = rs.getObject("telegram_message_id") == null ? null : Long.valueOf(rs.getLong("telegram_message_id"));
            return new AdminExpenseRow(
                    rs.getObject("id", UUID.class),
                    rs.getLong("amount_minor"),
                    ExpenseStatus.valueOf(rs.getString("status")),
                    rs.getString("payer_display_name"),
                    rs.getString("created_by_display_name"),
                    rs.getString("updated_by_display_name"),
                    rs.getString("comment"),
                    rs.getString("source_message_text"),
                    telegramChatId,
                    telegramMessageId,
                    rs.getObject("created_at", OffsetDateTime.class),
                    rs.getObject("updated_at", OffsetDateTime.class)
            );
        }
    };

    private static final RowMapper<AdminExpenseShareRow> EXPENSE_SHARE_ROW_MAPPER = new RowMapper<AdminExpenseShareRow>() {
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
     * Intermediate JDBC row for expense headers.
     */
    private static class AdminExpenseRow {
        private final UUID expenseId;
        private final long amountMinor;
        private final ExpenseStatus status;
        private final String payerDisplayName;
        private final String createdByDisplayName;
        private final String updatedByDisplayName;
        private final String comment;
        private final String sourceMessageText;
        private final Long telegramChatId;
        private final Long telegramMessageId;
        private final OffsetDateTime createdAt;
        private final OffsetDateTime updatedAt;

        /**
         * Creates a new admin expense row instance.
         */
        AdminExpenseRow(
                UUID expenseId,
                long amountMinor,
                ExpenseStatus status,
                String payerDisplayName,
                String createdByDisplayName,
                String updatedByDisplayName,
                String comment,
                String sourceMessageText,
                Long telegramChatId,
                Long telegramMessageId,
                OffsetDateTime createdAt,
                OffsetDateTime updatedAt) {
            this.expenseId = expenseId;
            this.amountMinor = amountMinor;
            this.status = status;
            this.payerDisplayName = payerDisplayName;
            this.createdByDisplayName = createdByDisplayName;
            this.updatedByDisplayName = updatedByDisplayName;
            this.comment = comment;
            this.sourceMessageText = sourceMessageText;
            this.telegramChatId = telegramChatId;
            this.telegramMessageId = telegramMessageId;
            this.createdAt = createdAt;
            this.updatedAt = updatedAt;
        }

        /**
         * Returns the expense id.
         */
        public UUID getExpenseId() {
            return expenseId;
        }

        /**
         * Returns the amount in minor units.
         */
        public long getAmountMinor() {
            return amountMinor;
        }

        /**
         * Returns the expense status.
         */
        public ExpenseStatus getStatus() {
            return status;
        }

        /**
         * Returns payer display name.
         */
        public String getPayerDisplayName() {
            return payerDisplayName;
        }

        /**
         * Returns creator display name.
         */
        public String getCreatedByDisplayName() {
            return createdByDisplayName;
        }

        /**
         * Returns updater display name.
         */
        public String getUpdatedByDisplayName() {
            return updatedByDisplayName;
        }

        /**
         * Returns the comment.
         */
        public String getComment() {
            return comment;
        }

        /**
         * Returns raw Telegram source message text.
         */
        public String getSourceMessageText() {
            return sourceMessageText;
        }

        /**
         * Returns Telegram chat id.
         */
        public Long getTelegramChatId() {
            return telegramChatId;
        }

        /**
         * Returns Telegram message id.
         */
        public Long getTelegramMessageId() {
            return telegramMessageId;
        }

        /**
         * Returns creation timestamp.
         */
        public OffsetDateTime getCreatedAt() {
            return createdAt;
        }

        /**
         * Returns update timestamp.
         */
        public OffsetDateTime getUpdatedAt() {
            return updatedAt;
        }
    }

    /**
     * Intermediate JDBC row for expense shares.
     */
    private static class AdminExpenseShareRow {
        private final UUID expenseId;
        private final UUID participantId;
        private final String participantDisplayName;
        private final long shareMinor;

        /**
         * Creates a new admin expense share row instance.
         */
        AdminExpenseShareRow(UUID expenseId, UUID participantId, String participantDisplayName, long shareMinor) {
            this.expenseId = expenseId;
            this.participantId = participantId;
            this.participantDisplayName = participantDisplayName;
            this.shareMinor = shareMinor;
        }

        /**
         * Returns the expense id.
         */
        public UUID getExpenseId() {
            return expenseId;
        }

        /**
         * Returns the participant id.
         */
        public UUID getParticipantId() {
            return participantId;
        }

        /**
         * Returns the participant display name.
         */
        public String getParticipantDisplayName() {
            return participantDisplayName;
        }

        /**
         * Returns the share amount in minor units.
         */
        public long getShareMinor() {
            return shareMinor;
        }
    }
}
