package ru.splitus.check.jdbc;

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
import ru.splitus.check.Participant;
import ru.splitus.check.ParticipantRepository;
import ru.splitus.check.ParticipantType;

@Repository
public class JdbcParticipantRepository implements ParticipantRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public JdbcParticipantRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Participant save(Participant participant) {
        MapSqlParameterSource parameters = new MapSqlParameterSource()
                .addValue("id", participant.getId())
                .addValue("checkId", participant.getCheckId())
                .addValue("participantType", participant.getType().name())
                .addValue("displayName", participant.getDisplayName())
                .addValue("linkedUserId", participant.getLinkedUserId())
                .addValue("mergedIntoParticipantId", participant.getMergedIntoParticipantId())
                .addValue("createdAt", participant.getCreatedAt());
        jdbcTemplate.update(
                "insert into participant(id, check_id, participant_type, display_name, linked_user_id, merged_into_participant_id, created_at) "
                        + "values (:id, :checkId, :participantType, :displayName, :linkedUserId, :mergedIntoParticipantId, :createdAt)",
                parameters
        );
        return participant;
    }

    @Override
    public int countByCheckId(UUID checkId) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from participant where check_id = :checkId and merged_into_participant_id is null",
                new MapSqlParameterSource("checkId", checkId),
                Integer.class
        );
        return count == null ? 0 : count.intValue();
    }

    @Override
    public boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName) {
        Integer count = jdbcTemplate.queryForObject(
                "select count(*) from participant where check_id = :checkId and display_name = :displayName and merged_into_participant_id is null",
                new MapSqlParameterSource().addValue("checkId", checkId).addValue("displayName", displayName),
                Integer.class
        );
        return count != null && count.intValue() > 0;
    }

    @Override
    public Optional<Participant> findActiveRegisteredParticipant(UUID checkId, UUID userId) {
        return jdbcTemplate.query(
                "select id, check_id, participant_type, display_name, linked_user_id, merged_into_participant_id, created_at "
                        + "from participant where check_id = :checkId and linked_user_id = :userId and merged_into_participant_id is null",
                new MapSqlParameterSource().addValue("checkId", checkId).addValue("userId", userId),
                PARTICIPANT_ROW_MAPPER
        ).stream().findFirst();
    }

    @Override
    public List<Participant> findByCheckId(UUID checkId) {
        return jdbcTemplate.query(
                "select id, check_id, participant_type, display_name, linked_user_id, merged_into_participant_id, created_at "
                        + "from participant where check_id = :checkId order by created_at asc",
                new MapSqlParameterSource("checkId", checkId),
                PARTICIPANT_ROW_MAPPER
        );
    }

    private static final RowMapper<Participant> PARTICIPANT_ROW_MAPPER = new RowMapper<Participant>() {
        @Override
        public Participant mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Participant(
                    rs.getObject("id", UUID.class),
                    rs.getObject("check_id", UUID.class),
                    ParticipantType.valueOf(rs.getString("participant_type")),
                    rs.getString("display_name"),
                    rs.getObject("linked_user_id", UUID.class),
                    rs.getObject("merged_into_participant_id", UUID.class),
                    rs.getObject("created_at", OffsetDateTime.class)
            );
        }
    };
}
