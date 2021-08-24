package no.nav.familie.integrasjoner.journalpost.internal

data class SafError(
        val message: String? = null,
        val code: SafErrorCode? = null,
        val exceptionType: String? = null,
        val exception: String? = null)

enum class SafErrorCode {
    forbidden,
    not_found,
    bad_request,
    server_error
}