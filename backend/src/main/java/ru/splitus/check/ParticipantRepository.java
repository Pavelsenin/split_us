package ru.splitus.check;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Defines persistence operations for participant.
 */
public interface ParticipantRepository {

    /**
     * Executes save.
     */
    Participant save(Participant participant);

    /**
     * Executes update.
     */
    Participant update(Participant participant);

    /**
     * Counts by check id.
     */
    int countByCheckId(UUID checkId);

    /**
     * Checks whether by check id and display name.
     */
    boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName);

    /**
     * Finds by id.
     */
    Optional<Participant> findById(UUID participantId);

    /**
     * Finds active registered participant.
     */
    Optional<Participant> findActiveRegisteredParticipant(UUID checkId, UUID userId);

    /**
     * Finds by check id.
     */
    List<Participant> findByCheckId(UUID checkId);
}



