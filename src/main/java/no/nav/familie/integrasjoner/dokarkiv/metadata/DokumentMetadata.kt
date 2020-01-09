package no.nav.familie.integrasjoner.dokarkiv.metadata

interface DokumentMetadata {
    val tema: String
    val behandlingstema: String?
    val kanal: String?
    val dokumentTypeId: String
    val tittel: String?
    val brevkode: String?
    val dokumentKategori: String
}