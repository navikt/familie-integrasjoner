package no.nav.familie.integrasjoner.journalpost.internal

import no.nav.familie.kontrakter.felles.Arkivtema

data class SafRequestVariabler(val journalpostId: String)

data class SafRequestVariablerForVedleggRequest(
    val tema: List<Arkivtema>?,
    val journalposttype: String?,
    val journalstatus: String?,
)

data class SafJournalpostRequest(
    val variables: Any,
    val query: String,
)
