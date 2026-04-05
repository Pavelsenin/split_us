package ru.splitus.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Captures a snapshot of check.
 */
public class CheckSnapshot {

    private final CheckBook checkBook;
    private final List<Participant> participants;

    /**
     * Creates a new check snapshot instance.
     */
    public CheckSnapshot(CheckBook checkBook, List<Participant> participants) {
        this.checkBook = checkBook;
        this.participants = Collections.unmodifiableList(new ArrayList<Participant>(participants));
    }

    /**
     * Returns the check book.
     */
    public CheckBook getCheckBook() {
        return checkBook;
    }

    /**
     * Returns the participants.
     */
    public List<Participant> getParticipants() {
        return participants;
    }
}



