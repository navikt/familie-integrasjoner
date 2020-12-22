package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdAutobrev6Og18ÅrMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "BA"
    override val tema: String = "BAR"
    override val behandlingstema: String? = null
    override val kanal: String? = null
    override val dokumentTypeId: String = "BARNETRYGD_AUTOBREV_6_OG_18_ÅR"
    override val tittel: String? = "Autobrev barnetrygd når barn fyller 6 og 18 år"
    override val brevkode: String? = "autobrev6_18år"
    override val dokumentKategori: String = "B"
}
