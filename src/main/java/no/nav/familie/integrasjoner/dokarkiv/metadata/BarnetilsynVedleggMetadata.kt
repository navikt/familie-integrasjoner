package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetilsynVedleggMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "ENF"
    override val behandlingstema: String? = null // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = null
    override val dokumentTypeId: String = "BARNETILSYNSTØNAD_VEDLEGG"
    override val tittel: String? = null
    override val brevkode: String? = null
    override val dokumentKategori: String = "IS"

}