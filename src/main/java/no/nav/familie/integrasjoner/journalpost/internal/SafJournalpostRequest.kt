package no.nav.familie.integrasjoner.journalpost.internal

val queryString: String = """
    query Journalpost(${"$"}journalpostId: String!) {
        journalpost(journalpostId: ${"$"}journalpostId) {
            journalpostId
            journalposttype
            journalstatus
            tema
            behandlingstema
            sak { arkivsaksystem arkivsaksnummer datoOpprettet fagsakId fagsaksystem }
            bruker { id, type }
            journalforendeEnhet
            kanal
            dokumenter { tittel brevkode dokumentstatus dokumentvarianter { variantformat } }
        }
    }
""".trimIndent()

data class SafRequestVariable(var journalpostId: String)

data class SafJournalpostRequest(val variables: SafRequestVariable,
                                 val query: String = queryString)