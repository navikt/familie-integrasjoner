package no.nav.familie.integrasjoner.enhet

data class SaksbehandlerId(
    val verdi: String
) {

    init {
        if (verdi.isBlank()) {
            throw IllegalStateException("Verdi kan ikke være tom")
        }
    }

}