package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdVedtakMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "FS36" //TODO: find out a real id
    override val tema: String = "BAR"
    override val behandlingstema: String? = "ab0270" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = ""
    override val dokumentTypeId: String = "BARNETRYGD_VEDTAK"
    override val tittel: String? = "Vedtak om innvilgelse av barnetrygd"
    override val brevkode: String? = "NAV xxxx" //TODO: order a brevkode
    override val dokumentKategori: String = "VB"

}