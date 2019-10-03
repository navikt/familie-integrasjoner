package no.nav.familie.ks.oppslag.journalpost.internal;

public class Journalpost {

    private String journalpostId;
    private Sak sak;

    public Journalpost() {
    }

    public Journalpost(String journalpostId, Sak sak) {
        this.journalpostId = journalpostId;
        this.sak = sak;
    }

    public String getJournalpostId() {
        return journalpostId;
    }

    public Sak getSak() {
        return sak;
    }
}
