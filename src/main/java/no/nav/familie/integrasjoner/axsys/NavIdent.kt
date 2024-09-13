package no.nav.familie.integrasjoner.axsys

data class NavIdent(
    val ident: String,
) {
    init {
        if (ident.isBlank()) {
            throw IllegalStateException("Verdi kan ikke være tom")
        }
    }
}
