package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdVedtakVedleggMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "BAR"
    override val behandlingstema: String? = null
    override val kanal: String? = null
    override val dokumentTypeId: String = "BARNETRYGD_VEDTAK_VEDLEGG"
    override val tittel: String? = null
    override val brevkode: String? = null
    override val dokumentKategori: String = "????"

}