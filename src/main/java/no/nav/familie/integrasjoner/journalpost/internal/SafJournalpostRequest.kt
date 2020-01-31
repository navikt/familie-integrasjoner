package no.nav.familie.integrasjoner.journalpost.internal

data class SafJournalpostRequest(val variables: SafRequestVariable,
                                 val query: String =
                                         "query Journalpost(\$journalpostId: String!) {journalpost(journalpostId: \$journalpostId) " +
                                         "{journalpostId sak {arkivsaksystem arkivsaksnummer datoOpprettet}}}")