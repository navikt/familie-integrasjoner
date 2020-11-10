package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdVarselOmRevurderingMetadata : DokumentMetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "BA"
    override val tema: String = "BAR"
    override val behandlingstema: String? = null
    override val kanal: String? = null
    override val dokumentTypeId: String = "BARNETRYGD_VARSEL_OM_REVURDERING"
    override val tittel: String? = "Brev for varsel om revurdering"
    override val brevkode: String? = "varsel-om-revurdering"
    override val dokumentKategori: String = "B"
}