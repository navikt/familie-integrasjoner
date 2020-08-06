package no.nav.familie.integrasjoner.journalpost.internal

import no.nav.familie.kontrakter.felles.journalpost.Journalpost

class SafJournalpostBrukerData(val dokumentoversiktBruker: Journalpostliste)

class Journalpostliste(val journalposter: List<Journalpost>)
