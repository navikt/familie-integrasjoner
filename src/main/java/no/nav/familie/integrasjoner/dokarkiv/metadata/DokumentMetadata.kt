package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType

interface DokumentMetadata {
    val journalpostType: JournalpostType
    val fagsakSystem: String?
    val tema: String
    val behandlingstema: String?
    val kanal: String?
    val dokumentTypeId: String
    val tittel: String?
    val brevkode: String?
    val dokumentKategori: String
}