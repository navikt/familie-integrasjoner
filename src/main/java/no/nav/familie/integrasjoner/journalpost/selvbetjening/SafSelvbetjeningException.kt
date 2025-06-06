package no.nav.familie.integrasjoner.journalpost.selvbetjening

class SafSelvbetjeningException(
    message: String?,
    override val cause: Throwable? = null,
) : RuntimeException(message, cause)
