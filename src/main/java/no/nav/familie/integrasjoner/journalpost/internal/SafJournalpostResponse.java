package no.nav.familie.integrasjoner.journalpost.internal;

import java.util.List;

public class SafJournalpostResponse {

    private SafJournalpostData data;
    private List<SafError> errors;

    public SafJournalpostData getData() {
        return data;
    }

    public List<SafError> getErrors() {
        return errors;
    }

    public boolean harFeil() {
        return errors != null && !errors.isEmpty();
    }
}
