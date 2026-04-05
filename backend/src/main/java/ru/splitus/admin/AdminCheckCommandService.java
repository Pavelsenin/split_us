package ru.splitus.admin;

import java.util.UUID;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Service;

/**
 * Coordinates admin check command operations.
 */
@Service
public class AdminCheckCommandService {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    /**
     * Creates a new admin check command service instance.
     */
    public AdminCheckCommandService(NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Deletes check.
     */
    public boolean deleteCheck(UUID checkId) {
        return jdbcTemplate.update(
                "delete from check_book where id = :checkId",
                new MapSqlParameterSource("checkId", checkId)
        ) > 0;
    }
}
