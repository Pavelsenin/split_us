package ru.splitus.check;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ParticipantRepository {

    Participant save(Participant participant);

    Participant update(Participant participant);

    int countByCheckId(UUID checkId);

    boolean existsByCheckIdAndDisplayName(UUID checkId, String displayName);

    Optional<Participant> findById(UUID participantId);

    Optional<Participant> findActiveRegisteredParticipant(UUID checkId, UUID userId);

    List<Participant> findByCheckId(UUID checkId);
}
