package no.nav.familie.integrasjoner.journalpost.internal

import no.nav.familie.kontrakter.felles.Arkivtema
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype

data class SafRequestVariabler(val journalpostId: String)

data class SafRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val journalposttype: String?,
    val journalstatus: List<String>?,
    val antall: Int = 200,
)

data class SafRequestForBruker(
    val brukerId: Bruker,
    val antall: Int,
    val tema: List<Tema>?,
    val journalposttype: List<Journalposttype>?,
    val journalstatus: List<String>? = emptyList(), // Dersom denne ikke blir sendt med så filtrerer SAF på journalposter som er ferdigstilte
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
            journalstatus = journalpostStatus?.let { listOf(it) },
            antall = antall,
        )
    }
}

fun JournalposterForBrukerRequest.tilSafRequestForBruker(): SafRequestForBruker {
    return SafRequestForBruker(
        brukerId = brukerId,
        tema = tema,
        journalposttype = journalposttype,
        antall = antall,
    )
}
