package no.nav.familie.ks.oppslag.journalpost.internal;

import java.util.List;

class SafJournalpostResponse {
    private SafJournalpostData data;
    private List<SafError> errors;

    public SafJournalpostResponse() {
    }

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
