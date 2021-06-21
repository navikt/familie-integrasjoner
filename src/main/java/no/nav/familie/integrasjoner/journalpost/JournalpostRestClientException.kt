package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest


class JournalpostRestClientException(message: String?, cause: Throwable?, val journalpostId: String) :
        RuntimeException(message, cause)

class JournalpostForBrukerException(message: String?,
                                    cause: Throwable?,
                                    val journalposterForBrukerRequest: JournalposterForBrukerRequest) :
        RuntimeException(message, cause)

class JournalpostForbiddenException(message: String?, cause: Throwable?) : RuntimeException(message, cause)