package ru.splitus.admin;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Provides read-only admin views over checks and their related data.
 */
public interface AdminReadService {

    /**
     * Searches checks for the admin dashboard.
     *
     * @param query free-form search query; blank value returns recent checks
     * @return matching checks ordered for admin inspection
     */
    List<AdminCheckSummary> searchChecks(String query);

    /**
     * Loads a full read-only check view for the admin detail page.
     *
     * @param checkId target check identifier
     * @return populated detail view when the check exists
     */
    Optional<AdminCheckDetails> findCheckDetails(UUID checkId);
}
