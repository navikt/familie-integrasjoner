package no.nav.familie.integrasjoner.journalpost.internal

import no.nav.familie.kontrakter.felles.Arkivtema
import no.nav.familie.kontrakter.felles.journalpost.Bruker

data class SafRequestVariabler(val journalpostId: String)

data class SafRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val journalposttype: String?,
    val journalstatus: String?,
    val antall: Int = 200,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)

data class JournalposterForVedleggRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val dokumenttype: String?,
    val journalpostStatus: String?,
    val antall: Int = 200,
) {
    fun tilSafRequest(): SafRequest {
        return SafRequest(
            brukerId = brukerId,
            tema = tema,
            journalposttype = dokumenttype,
            journalstatus = journalpostStatus,
            antall = antall
        )
    }
}
