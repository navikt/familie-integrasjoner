package no.nav.familie.integrasjoner.journalpost

class JournalpostRestClientException(message: String?, cause: Throwable?, val journalpostId: String) :
        RuntimeException(message, cause)