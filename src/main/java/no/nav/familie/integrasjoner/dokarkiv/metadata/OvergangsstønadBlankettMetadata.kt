package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object OvergangsstønadBlankettMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.NOTAT
    override val fagsakSystem: String = "IT01"
    override val tema: String = "ENF"
    override val behandlingstema: String = "ab0071"
    override val kanal: String? = null
    override val dokumentTypeId: String = "OVERGANGSSTØNAD_BLANKETT"
    override val tittel: String = "Blankett for overgangsstønad - enslig mor eller far"
    override val brevkode: String? = null
    override val dokumentKategori: String = "FORVALTNINGSNOTAT"

}