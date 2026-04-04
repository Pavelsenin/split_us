package ru.splitus.check;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CheckSnapshot {

    private final CheckBook checkBook;
    private final List<Participant> participants;

    public CheckSnapshot(CheckBook checkBook, List<Participant> participants) {
        this.checkBook = checkBook;
        this.participants = Collections.unmodifiableList(new ArrayList<Participant>(participants));
    }

    public CheckBook getCheckBook() {
        return checkBook;
    }

    public List<Participant> getParticipants() {
        return participants;
    }
}
