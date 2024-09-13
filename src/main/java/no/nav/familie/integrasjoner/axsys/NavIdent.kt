package no.nav.familie.integrasjoner.axsys

data class NavIdent(
    val ident: String,
) {
    init {
        if (ident.isBlank()) {
            throw IllegalStateException("Ident kan ikke være tom")
        }
    }
}
