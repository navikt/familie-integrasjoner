package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetrygdSøknadOrdinærMetadata
    : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "BAR"
    override val behandlingstema: String? = "ab0180" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "BARNETRYGD_ORDINÆR"
    override val tittel: String? = "Søknad om barnetrygd ordinær"
    override val brevkode: String? = "NAV 33-00.07"
    override val dokumentKategori: String = "SOK"

}