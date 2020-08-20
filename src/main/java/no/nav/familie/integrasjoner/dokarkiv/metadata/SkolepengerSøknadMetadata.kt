package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object SkolepengerSøknadMetadata : DokumentMetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "ENF"
    override val behandlingstema: String? = "ab0177" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "SKOLEPENGER_SØKNAD"
    override val tittel: String? = "Søknad om skolepenger - enslig mor eller far"
    override val brevkode: String? = "NAV 15-00.04"
    override val dokumentKategori: String = "SOK"

}