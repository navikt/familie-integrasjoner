package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdSøknadEØSMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "BAR"
    override val behandlingstema: String? = "ab0058" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "BARNETRYGD_EØS"
    override val tittel: String? = "Tilleggsskjema ved krav om utbetaling av barnetrygd og/eller kontantstøtte på grunnlag av regler om eksport etter EØS-avtalen"
    override val brevkode: String? = "NAV 34-00.15"
    override val dokumentKategori: String = "SOK"

}