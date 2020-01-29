package no.nav.familie.integrasjoner.journalpost.internal

data class Journalpost(
        val journalpostId: String,
        val sak: Sak? = null
)