package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object OvergangsstønadBlankettMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.NOTAT
    override val fagsakSystem: String = "Infotrygd"
    override val tema: String = "ENF"
    override val behandlingstema: String = "ab0071" // Overgangsstønad
    override val kanal: String? = null
    override val dokumentTypeId: String = "OVERGANGSSTØNADBLANKETT"
    override val tittel: String = "Blankett - enslig mor eller far"
    override val brevkode: String? = null
    override val dokumentKategori: String = "FORVALTNINGSNOTAT"

}