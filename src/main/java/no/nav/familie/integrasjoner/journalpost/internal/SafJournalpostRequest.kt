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
            bruker { id type }
            journalforendeEnhet
            kanal
            dokumenter { dokumentInfoId tittel brevkode dokumentstatus dokumentvarianter { variantformat } logiskeVedlegg { logiskVedleggId tittel } }
            relevanteDatoer { dato datotype }
        }
    }
""".trimIndent()

data class SafRequestVariable(var journalpostId: String)

data class SafJournalpostRequest(val variables: SafRequestVariable,
                                 val query: String = queryString)