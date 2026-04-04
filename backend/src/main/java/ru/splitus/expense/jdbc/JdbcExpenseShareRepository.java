package ru.splitus.expense.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;

@Repository
public class JdbcExpenseShareRepository implements ExpenseShareRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcExpenseShareRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void saveAll(List<ExpenseShare> shares) {
        if (shares.isEmpty()) {
            return;
        }

        jdbcTemplate.getJdbcTemplate().batchUpdate(
                "insert into expense_share(expense_id, participant_id, share_minor) values (?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(java.sql.PreparedStatement ps, int i) throws SQLException {
                        ExpenseShare share = shares.get(i);
                        ps.setObject(1, share.getExpenseId());
                        ps.setObject(2, share.getParticipantId());
                        ps.setLong(3, share.getShareMinor());
                    }

                    @Override
                    public int getBatchSize() {
                        return shares.size();
                    }
                }
        );
    }

    @Override
    public List<ExpenseShare> findByExpenseId(UUID expenseId) {
        return jdbcTemplate.query(
                "select expense_id, participant_id, share_minor from expense_share where expense_id = :expenseId order by participant_id asc",
                new MapSqlParameterSource("expenseId", expenseId),
                EXPENSE_SHARE_ROW_MAPPER
        );
    }

    @Override
    public void deleteByExpenseId(UUID expenseId) {
        jdbcTemplate.update("delete from expense_share where expense_id = :expenseId", new MapSqlParameterSource("expenseId", expenseId));
    }

    private static final RowMapper<ExpenseShare> EXPENSE_SHARE_ROW_MAPPER = new RowMapper<ExpenseShare>() {
        @Override
        public ExpenseShare mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExpenseShare(
                    rs.getObject("expense_id", UUID.class),
                    rs.getObject("participant_id", UUID.class),
                    rs.getLong("share_minor")
            );
        }
    };
}
