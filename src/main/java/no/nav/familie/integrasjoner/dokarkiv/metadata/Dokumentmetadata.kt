package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype

interface Dokumentmetadata {

    val journalpostType: JournalpostType
    val fagsakSystem: Fagsystem?
    val tema: Tema
    val behandlingstema: Behandlingstema?
    val kanal: String?
    val dokumenttype: Dokumenttype
    val tittel: String?
    val brevkode: String?
    val dokumentKategori: Dokumentkategori
}

enum class Dokumentkategori(private val beskrivelse: String) {
    B("Brev"),
    VB("Vedtaksbrev"),
    IB("Infobrev"),
    ES("Elektronisk skjema"),
    TS("Tolkbart skjema"),
    IS("Ikke tolkbart skjema"),
    KS("Konverterte data fra system"),
    KD("Konvertert fra elektronisk arkiv"),
    SED("SED"),
    PUBL_BLANKETT_EOS("Pb EØS"),
    ELEKTRONISK_DIALOG("Elektronisk dialog"),
    REFERAT("Referat"),
    FORVALTNINGSNOTAT("Forvaltningsnotat"), // DENNE BLIR SYNLIG TIL SLUTTBRUKER!
    SOK("Søknad"),
    KA("Klage eller anke")
}
