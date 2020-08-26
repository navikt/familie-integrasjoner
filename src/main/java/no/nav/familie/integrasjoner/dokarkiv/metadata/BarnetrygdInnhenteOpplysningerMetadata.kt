package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdInnhenteOpplysningerMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "BA"
    override val tema: String = "BAR"
    override val behandlingstema: String? = null
    override val kanal: String? = null
    override val dokumentTypeId: String = "BARNETRYGD_INNHENTE_OPPLYSNINGER"
    override val tittel: String? = "Brev for innhenting av opplysninger"
    override val brevkode: String? = "innhente-opplysninger"
    override val dokumentKategori: String = "B"


}