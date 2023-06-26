package no.nav.familie.integrasjoner.journalpost.internal

import no.nav.familie.kontrakter.felles.Arkivtema
import no.nav.familie.kontrakter.felles.journalpost.Bruker

data class SafRequestVariabler(val journalpostId: String)

data class SafRequestVariablerForVedleggRequest(
    val brukerId: Bruker,
    val tema: List<Arkivtema>?,
    val journalposttype: String?,
    val journalstatus: String?,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)
