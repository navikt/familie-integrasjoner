package no.nav.familie.integrasjoner.journalpost.internal

data class SafJournalpostResponse<T>(
    val data: T? = null,
    val errors: List<SafError>? = null,
) {
    fun harFeil(): Boolean = errors != null && errors.isNotEmpty()
}
