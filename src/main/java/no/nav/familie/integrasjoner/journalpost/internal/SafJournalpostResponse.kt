package no.nav.familie.integrasjoner.journalpost.internal

data class SafJournalpostResponse (
    val data: SafJournalpostData? = null,
    val errors: List<SafError>? = null) {

    fun harFeil(): Boolean {
        return errors != null && errors.isNotEmpty()
    }
}
