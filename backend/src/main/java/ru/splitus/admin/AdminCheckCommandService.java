package ru.splitus.admin;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.splitus.check.CheckBook;
import ru.splitus.check.CheckBookRepository;
import ru.splitus.error.ApiErrorCode;
import ru.splitus.error.ApiException;

/**
 * Performs destructive admin operations over checks.
 */
@Service
public class AdminCheckCommandService {

    private static final Logger log = LoggerFactory.getLogger(AdminCheckCommandService.class);

    private final CheckBookRepository checkBookRepository;
    private final MeterRegistry meterRegistry;

    /**
     * Creates a new admin check command service instance.
     */
    public AdminCheckCommandService(CheckBookRepository checkBookRepository) {
        this(checkBookRepository, new SimpleMeterRegistry());
    }

    /**
     * Creates a new admin check command service instance.
     */
    @Autowired
    public AdminCheckCommandService(CheckBookRepository checkBookRepository, MeterRegistry meterRegistry) {
        this.checkBookRepository = checkBookRepository;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Deletes a check after explicit title confirmation.
     */
    @Transactional
    public String deleteCheck(UUID checkId, String confirmationTitle) {
        CheckBook checkBook = checkBookRepository.findById(checkId)
                .orElseThrow(() -> new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Check not found"));
        String expectedTitle = checkBook.getTitle();
        String normalizedConfirmation = confirmationTitle == null ? "" : confirmationTitle.trim();
        if (!expectedTitle.equals(normalizedConfirmation)) {
            meterRegistry.counter("splitus.admin.check.delete.total", "outcome", "rejected").increment();
            log.warn("Admin check delete rejected because confirmation title mismatched: checkId={}", checkId);
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Confirmation title does not match the check title");
        }
        if (!checkBookRepository.deleteById(checkId)) {
            meterRegistry.counter("splitus.admin.check.delete.total", "outcome", "missing").increment();
            throw new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Check not found");
        }
        meterRegistry.counter("splitus.admin.check.delete.total", "outcome", "success").increment();
        log.info("Admin deleted check: checkId={} title={}", checkId, expectedTitle);
        return expectedTitle;
    }
}
