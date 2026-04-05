package ru.splitus.check.jdbc;

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.splitus.check.ParticipantMergeRecord;
import ru.splitus.check.ParticipantMergeRepository;

/**
 * Represents jdbc participant merge repository.
 */
@Repository
public class JdbcParticipantMergeRepository implements ParticipantMergeRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new jdbc participant merge repository instance.
     */
    public JdbcParticipantMergeRepository(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Executes save.
     */
    @Override
    public ParticipantMergeRecord save(ParticipantMergeRecord record) {
        jdbcTemplate.update(
                "insert into participant_merge(id, check_id, source_participant_id, target_participant_id, performed_by_participant_id, created_at) "
                        + "values (:id, :checkId, :sourceParticipantId, :targetParticipantId, :performedByParticipantId, :createdAt)",
                new MapSqlParameterSource()
                        .addValue("id", record.getId())
                        .addValue("checkId", record.getCheckId())
                        .addValue("sourceParticipantId", record.getSourceParticipantId())
                        .addValue("targetParticipantId", record.getTargetParticipantId())
                        .addValue("performedByParticipantId", record.getPerformedByParticipantId())
                        .addValue("createdAt", record.getCreatedAt())
        );
        return record;
    }
}



