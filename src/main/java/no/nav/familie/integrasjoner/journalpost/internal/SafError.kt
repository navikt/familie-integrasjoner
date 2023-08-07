@file:Suppress("ktlint:standard:enum-entry-name-case")

package no.nav.familie.integrasjoner.journalpost.internal

data class SafError(
    val message: String,
    val extensions: SafExtension,
)

data class SafExtension(
    val code: SafErrorCode,
    val classification: String,
)

@Suppress("EnumEntryName")
enum class SafErrorCode {
    forbidden,
    not_found,
    bad_request,
    server_error,
}
