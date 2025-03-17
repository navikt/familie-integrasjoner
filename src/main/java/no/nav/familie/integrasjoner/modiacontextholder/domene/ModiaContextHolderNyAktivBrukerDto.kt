package no.nav.familie.integrasjoner.modiacontextholder.domene

import no.nav.familie.kontrakter.felles.Fødselsnummer

data class ModiaContextHolderNyAktivBrukerDto(
    val personIdent: String,
) {
    init {
        Fødselsnummer(personIdent)
    }

    fun toRequest(): ModiaContextHolderRequest =
        ModiaContextHolderRequest(
            verdi = personIdent,
            eventType = ModiaContextEventType.NY_AKTIV_BRUKER,
        )
}
