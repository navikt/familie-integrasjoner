package no.nav.familie.ks.oppslag.journalpost;


public class JournalpostRestClientException extends RuntimeException {
    private String journalpostId;


    public JournalpostRestClientException(String message, Throwable cause, String journalpostId) {
        super(message, cause);
        this.journalpostId = journalpostId;
    }

    public String getJournalpostId() {
        return journalpostId;
    }
}
