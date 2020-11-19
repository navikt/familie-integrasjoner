package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdHenleggelseMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "BA"
    override val tema: String = "BAR"
    override val behandlingstema: String? = null
    override val kanal: String? = null
    override val dokumentTypeId: String = "BARNETRYGD_HENLEGGELSE"
    override val tittel: String? = "Brev for bekreftelse av trukket søknad"
    override val brevkode: String? = "henleggelse"
    override val dokumentKategori: String = "B"


}
