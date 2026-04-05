package ru.splitus.admin;

import java.util.UUID;
import org.springframework.http.HttpStatus;
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

    private final CheckBookRepository checkBookRepository;

    /**
     * Creates a new admin check command service instance.
     */
    public AdminCheckCommandService(CheckBookRepository checkBookRepository) {
        this.checkBookRepository = checkBookRepository;
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
            throw new ApiException(ApiErrorCode.VALIDATION_ERROR, HttpStatus.BAD_REQUEST, "Confirmation title does not match the check title");
        }
        if (!checkBookRepository.deleteById(checkId)) {
            throw new ApiException(ApiErrorCode.CHECK_NOT_FOUND, HttpStatus.NOT_FOUND, "Check not found");
        }
        return expectedTitle;
    }
}
