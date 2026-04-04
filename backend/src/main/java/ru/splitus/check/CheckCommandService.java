package ru.splitus.check;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;
import ru.splitus.expense.Expense;
import ru.splitus.expense.ExpenseRepository;
import ru.splitus.expense.ExpenseShare;
import ru.splitus.expense.ExpenseShareRepository;

@Service
public class CheckCommandService {

    private static final int MAX_CHECKS_PER_24_HOURS = 100;
    private static final int MAX_PARTICIPANTS_PER_CHECK = 50;
    private static final String RUB = "RUB";

    private final AppUserRepository appUserRepository;
    private final CheckBookRepository checkBookRepository;
    private final ParticipantRepository participantRepository;
    private final ParticipantMergeRepository participantMergeRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseShareRepository expenseShareRepository;

    public CheckCommandService(
            AppUserRepository appUserRepository,
            CheckBookRepository checkBookRepository,
            ParticipantRepository participantRepository,
            ParticipantMergeRepository participantMergeRepository,
            ExpenseRepository expenseRepository,
            ExpenseShareRepository expenseShareRepository) {
        this.appUserRepository = appUserRepository;
        this.checkBookRepository = checkBookRepository;
        this.participantRepository = participantRepository;
        this.participantMergeRepository = participantMergeRepository;
        this.expenseRepository = expenseRepository;
        this.expenseShareRepository = expenseShareRepository;
    }

    @Transactional
    public CheckSnapshot createCheck(String title, long ownerTelegramUserId, String ownerTelegramUsername) {
        String normalizedTitle = requireNonBlank(title, "Название чека обязательно");
        String normalizedUsername = normalizeTelegramUsername(ownerTelegramUsername);

        AppUser owner = upsertUser(ownerTelegramUserId, normalizedUsername);
        ensureCheckCreationLimit(owner.getId());

        OffsetDateTime now = OffsetDateTime.now();
        CheckBook checkBook = new CheckBook(UUID.randomUUID(), normalizedTitle, owner.getId(), null, RUB, true, now);
        checkBookRepository.save(checkBook);

        Participant ownerParticipant = new Participant(
                UUID.randomUUID(),
                checkBook.getId(),
                ParticipantType.REGISTERED,
                normalizedUsername,
                owner.getId(),
                null,
                now
        );
        participantRepository.save(ownerParticipant);
        return new CheckSnapshot(checkBook, java.util.Collections.singletonList(ownerParticipant));
    }

    @Transactional(readOnly = true)
    public CheckSnapshot getCheck(UUID checkId) {
        CheckBook checkBook = loadCheck(checkId);
        List<Participant> participants = participantRepository.findByCheckId(checkId);
        return new CheckSnapshot(checkBook, participants);
    }

    @Transactional
    public Participant addGuestParticipant(UUID checkId, String displayName) {
        CheckBook checkBook = loadCheck(checkId);
        String normalizedDisplayName = requireNonBlank(displayName, "Имя участника обязательно");
        ensureParticipantCapacity(checkBook.getId());
        ensureParticipantNameAvailable(checkBook.getId(), normalizedDisplayName);

        Participant participant = new Participant(
                UUID.randomUUID(),
                checkBook.getId(),
                ParticipantType.GUEST,
                normalizedDisplayName,
                null,
                null,
                OffsetDateTime.now()
        );
        return participantRepository.save(participant);
    }

    @Transactional
    public Participant addRegisteredParticipant(UUID checkId, long telegramUserId, String telegramUsername) {
        CheckBook checkBook = loadCheck(checkId);
        String normalizedUsername = normalizeTelegramUsername(telegramUsername);
        AppUser user = upsertUser(telegramUserId, normalizedUsername);

        ensureParticipantCapacity(checkBook.getId());

        Optional<Participant> existingParticipant = participantRepository.findActiveRegisteredParticipant(checkBook.getId(), user.getId());
        if (existingParticipant.isPresent()) {
            throw new ApiException(
                    ApiErrorCode.REGISTERED_PARTICIPANT_ALREADY_EXISTS,
                    HttpStatus.CONFLICT,
                    "Пользователь уже добавлен в этот чек"
            );
        }

        ensureParticipantNameAvailable(checkBook.getId(), normalizedUsername);

        Participant participant = new Participant(
                UUID.randomUUID(),
                checkBook.getId(),
                ParticipantType.REGISTERED,
                normalizedUsername,
                user.getId(),
                null,
                OffsetDateTime.now()
        );
        return participantRepository.save(participant);
    }

    @Transactional
    public Participant mergeParticipant(UUID checkId, UUID sourceParticipantId, UUID targetParticipantId, UUID performedByParticipantId) {
        CheckBook checkBook = loadCheck(checkId);
        Map<UUID, Participant> participantMap = participantMap(checkId);

        Participant sourceParticipant = loadParticipant(participantMap, sourceParticipantId);
        Participant targetParticipant = loadParticipant(participantMap, targetParticipantId);
        loadParticipant(participantMap, performedByParticipantId);

        validateMergeParticipants(sourceParticipant, targetParticipant);

        participantMergeRepository.save(new ParticipantMergeRecord(
                UUID.randomUUID(),
                checkBook.getId(),
                sourceParticipant.getId(),
                targetParticipant.getId(),
                performedByParticipantId,
                OffsetDateTime.now()
        ));

        Participant mergedSource = new Participant(
                sourceParticipant.getId(),
                sourceParticipant.getCheckId(),
                sourceParticipant.getType(),
                sourceParticipant.getDisplayName(),
                sourceParticipant.getLinkedUserId(),
                targetParticipant.getId(),
                sourceParticipant.getCreatedAt()
        );
        participantRepository.update(mergedSource);

        reassignExpenseReferences(checkBook.getId(), sourceParticipant.getId(), targetParticipant.getId());
        return targetParticipant;
    }

    private AppUser upsertUser(long telegramUserId, String normalizedUsername) {
        Optional<AppUser> existingUser = appUserRepository.findByTelegramUserId(telegramUserId);
        if (!existingUser.isPresent()) {
            OffsetDateTime now = OffsetDateTime.now();
            AppUser newUser = new AppUser(UUID.randomUUID(), telegramUserId, normalizedUsername, now, now);
            return appUserRepository.save(newUser);
        }

        AppUser user = existingUser.get();
        if (!normalizedUsername.equals(user.getTelegramUsername())) {
            return appUserRepository.updateUsername(user, normalizedUsername);
        }
        return user;
    }

    private Participant loadParticipant(Map<UUID, Participant> participantMap, UUID participantId) {
        Participant participant = participantMap.get(participantId);
        if (participant == null) {
            throw new ApiException(ApiErrorCode.PARTICIPANT_NOT_FOUND, HttpStatus.NOT_FOUND, "Participant not found");
        }
        if (!participant.isActive()) {
            throw new ApiException(ApiErrorCode.PARTICIPANT_MERGE_INVALID, HttpStatus.CONFLICT, "Participant is already merged");
        }
        return participant;
    }

    private CheckBook loadCheck(UUID checkId) {
        return checkBookRepository.findById(checkId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Чек не найден"));
    }

    private Map<UUID, Participant> participantMap(UUID checkId) {
        List<Participant> participants = participantRepository.findByCheckId(checkId);
        Map<UUID, Participant> result = new LinkedHashMap<UUID, Participant>();
        for (Participant participant : participants) {
            result.put(participant.getId(), participant);
        }
        return result;
    }

    private void ensureCheckCreationLimit(UUID ownerUserId) {
        int currentCount = checkBookRepository.countCreatedByOwnerSince(ownerUserId, OffsetDateTime.now().minusHours(24));
        if (currentCount >= MAX_CHECKS_PER_24_HOURS) {
            throw new ApiException(
                    ApiErrorCode.CHECK_CREATION_LIMIT_REACHED,
                    HttpStatus.CONFLICT,
                    "Превышен лимит создания чеков за 24 часа"
            );
        }
    }

    private void ensureParticipantCapacity(UUID checkId) {
        int participantCount = participantRepository.countByCheckId(checkId);
        if (participantCount >= MAX_PARTICIPANTS_PER_CHECK) {
            throw new ApiException(
                    ApiErrorCode.PARTICIPANT_LIMIT_REACHED,
                    HttpStatus.CONFLICT,
                    "Достигнут лимит участников в чеке"
            );
        }
    }

    private void ensureParticipantNameAvailable(UUID checkId, String displayName) {
        if (participantRepository.existsByCheckIdAndDisplayName(checkId, displayName)) {
            throw new ApiException(
                    ApiErrorCode.PARTICIPANT_NAME_CONFLICT,
                    HttpStatus.CONFLICT,
                    "Имя участника уже используется в этом чеке"
            );
        }
    }

    private void validateMergeParticipants(Participant sourceParticipant, Participant targetParticipant) {
        if (sourceParticipant.getId().equals(targetParticipant.getId())) {
            throw new ApiException(ApiErrorCode.PARTICIPANT_MERGE_INVALID, HttpStatus.BAD_REQUEST, "Source and target participants must differ");
        }
        if (sourceParticipant.getType() != ParticipantType.GUEST) {
            throw new ApiException(ApiErrorCode.PARTICIPANT_MERGE_INVALID, HttpStatus.BAD_REQUEST, "Only guest participant can be merged as source");
        }
        if (targetParticipant.getType() != ParticipantType.REGISTERED) {
            throw new ApiException(ApiErrorCode.PARTICIPANT_MERGE_INVALID, HttpStatus.BAD_REQUEST, "Target participant must be registered");
        }
    }

    private void reassignExpenseReferences(UUID checkId, UUID sourceParticipantId, UUID targetParticipantId) {
        List<Expense> expenses = expenseRepository.findByCheckId(checkId);
        for (Expense expense : expenses) {
            boolean expenseChanged = false;
            UUID payerParticipantId = expense.getPayerParticipantId();
            UUID createdByParticipantId = expense.getCreatedByParticipantId();
            UUID updatedByParticipantId = expense.getUpdatedByParticipantId();

            if (sourceParticipantId.equals(payerParticipantId)) {
                payerParticipantId = targetParticipantId;
                expenseChanged = true;
            }
            if (sourceParticipantId.equals(createdByParticipantId)) {
                createdByParticipantId = targetParticipantId;
                expenseChanged = true;
            }
            if (sourceParticipantId.equals(updatedByParticipantId)) {
                updatedByParticipantId = targetParticipantId;
                expenseChanged = true;
            }

            if (expenseChanged) {
                expenseRepository.update(new Expense(
                        expense.getId(),
                        expense.getCheckId(),
                        expense.getAmountMinor(),
                        expense.getCurrencyCode(),
                        payerParticipantId,
                        expense.getComment(),
                        expense.getSourceMessageText(),
                        expense.getTelegramChatId(),
                        expense.getTelegramMessageId(),
                        expense.getStatus(),
                        createdByParticipantId,
                        updatedByParticipantId,
                        expense.getCreatedAt(),
                        expense.getUpdatedAt()
                ));
            }

            List<ExpenseShare> shares = expenseShareRepository.findByExpenseId(expense.getId());
            boolean sharesChanged = false;
            Map<UUID, Long> mergedShares = new LinkedHashMap<UUID, Long>();
            for (ExpenseShare share : shares) {
                UUID participantId = share.getParticipantId().equals(sourceParticipantId) ? targetParticipantId : share.getParticipantId();
                if (!participantId.equals(share.getParticipantId())) {
                    sharesChanged = true;
                }
                Long currentAmount = mergedShares.get(participantId);
                mergedShares.put(participantId, Long.valueOf((currentAmount == null ? 0L : currentAmount.longValue()) + share.getShareMinor()));
            }

            if (sharesChanged) {
                List<ExpenseShare> replacementShares = new ArrayList<ExpenseShare>();
                for (Map.Entry<UUID, Long> entry : mergedShares.entrySet()) {
                    replacementShares.add(new ExpenseShare(expense.getId(), entry.getKey(), entry.getValue().longValue()));
                }
                expenseShareRepository.deleteByExpenseId(expense.getId());
                expenseShareRepository.saveAll(replacementShares);
            }
        }
    }

    private String requireNonBlank(String value, String message) {
        if (value == null || value.trim().isEmpty()) {
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, message);
        }
        return value.trim();
    }

    private String normalizeTelegramUsername(String telegramUsername) {
        String normalized = requireNonBlank(telegramUsername, "Для зарегистрированного участника обязателен Telegram username");
        while (normalized.startsWith("@")) {
            normalized = normalized.substring(1);
        }
        if (normalized.isEmpty()) {
            throw new ApiException(
                    ApiErrorCode.VALIDATION_ERROR,
                    HttpStatus.BAD_REQUEST,
                    "Для зарегистрированного участника обязателен Telegram username"
            );
        }
        return normalized;
    }
}
