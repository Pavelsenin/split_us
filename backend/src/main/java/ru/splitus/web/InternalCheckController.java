package ru.splitus.web;

import java.net.URI;
import java.util.UUID;
import javax.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.splitus.check.CheckCommandService;
import ru.splitus.check.CheckSnapshot;
import ru.splitus.check.Participant;
import ru.splitus.web.dto.AddGuestParticipantRequest;
import ru.splitus.web.dto.AddRegisteredParticipantRequest;
import ru.splitus.web.dto.CheckResponse;
import ru.splitus.web.dto.CreateCheckRequest;
import ru.splitus.web.dto.MergeParticipantRequest;
import ru.splitus.web.dto.ParticipantResponse;

@RestController
@RequestMapping("/api/internal/checks")
public class InternalCheckController {

    private final CheckCommandService checkCommandService;

    public InternalCheckController(CheckCommandService checkCommandService) {
        this.checkCommandService = checkCommandService;
    }

    @PostMapping
    public ResponseEntity<CheckResponse> createCheck(@Valid @RequestBody CreateCheckRequest request) {
        CheckSnapshot snapshot = checkCommandService.createCheck(
                request.getTitle(),
                request.getOwnerTelegramUserId().longValue(),
                request.getOwnerTelegramUsername()
        );
        return ResponseEntity.created(URI.create("/api/internal/checks/" + snapshot.getCheckBook().getId()))
                .body(CheckResponse.fromDomain(snapshot));
    }

    @GetMapping("/{checkId}")
    public CheckResponse getCheck(@PathVariable UUID checkId) {
        return CheckResponse.fromDomain(checkCommandService.getCheck(checkId));
    }

    @PostMapping("/{checkId}/participants/guest")
    public ResponseEntity<ParticipantResponse> addGuestParticipant(
            @PathVariable UUID checkId,
            @Valid @RequestBody AddGuestParticipantRequest request) {
        Participant participant = checkCommandService.addGuestParticipant(checkId, request.getDisplayName());
        return ResponseEntity.created(URI.create("/api/internal/checks/" + checkId + "/participants/" + participant.getId()))
                .body(ParticipantResponse.fromDomain(participant));
    }

    @PostMapping("/{checkId}/participants/registered")
    public ResponseEntity<ParticipantResponse> addRegisteredParticipant(
            @PathVariable UUID checkId,
            @Valid @RequestBody AddRegisteredParticipantRequest request) {
        Participant participant = checkCommandService.addRegisteredParticipant(
                checkId,
                request.getTelegramUserId().longValue(),
                request.getTelegramUsername()
        );
        return ResponseEntity.created(URI.create("/api/internal/checks/" + checkId + "/participants/" + participant.getId()))
                .body(ParticipantResponse.fromDomain(participant));
    }

    @PostMapping("/{checkId}/participants/{sourceParticipantId}/merge")
    public ParticipantResponse mergeParticipant(
            @PathVariable UUID checkId,
            @PathVariable UUID sourceParticipantId,
            @Valid @RequestBody MergeParticipantRequest request) {
        return ParticipantResponse.fromDomain(checkCommandService.mergeParticipant(
                checkId,
                sourceParticipantId,
                request.getTargetParticipantId(),
                request.getPerformedByParticipantId()
        ));
    }
}
