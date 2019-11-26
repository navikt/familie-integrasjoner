package no.nav.familie.integrasjoner.journalpost.internal;

class SafRequestVariable {

    private String journalpostId;

    public String getJournalpostId() {
        return journalpostId;
    }

    public void setJournalpostId(String journalpostId) {
        this.journalpostId = journalpostId;
    }

    public SafRequestVariable(String journalpostId) {
        this.journalpostId = journalpostId;
    }
}
