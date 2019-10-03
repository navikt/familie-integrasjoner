package no.nav.familie.ks.oppslag.journalpost.internal;

class SafJournalpostRequest {
    private final String query = "query Journalpost($journalpostId: String!) {journalpost(journalpostId: $journalpostId) {journalpostId sak {arkivsaksystem arkivsaksnummer datoOpprettet}}}";
    private SafRequestVariable variables;

    public SafJournalpostRequest(SafRequestVariable variables) {
        this.variables = variables;
    }
}
