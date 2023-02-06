package no.nav.familie.integrasjoner.journalpost.internal

data class SafError(
    val message: String,
    val extensions: SafExtension,
)

data class SafExtension(
    val code: SafErrorCode,
    val classification: String,
)

/* ktlint-disable enum-entry-name-case */
@Suppress("EnumEntryName")
enum class SafErrorCode {
    forbidden,
    not_found,
    bad_request,
    server_error,
}
/* ktlint-enable enum-entry-name-case */
