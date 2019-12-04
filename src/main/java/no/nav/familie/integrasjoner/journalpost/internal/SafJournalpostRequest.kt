package no.nav.familie.integrasjoner.journalpost.internal

class SafJournalpostRequest(private val variables: SafRequestVariable) {
    private val query =
            "query Journalpost(\$journalpostId: String!) {journalpost(journalpostId: \$journalpostId) {journalpostId sak {arkivsaksystem arkivsaksnummer datoOpprettet}}}"

}