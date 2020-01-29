package no.nav.familie.integrasjoner.journalpost;


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
