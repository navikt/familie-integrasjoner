package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object ArbeidsregistreringsskjemaMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "ENF"
    override val behandlingstema: String? = null //"ab0071" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "SKJEMA_ARBEIDSSØKER"
    override val tittel: String? = "Enslig mor eller far som er arbeidssøker"
    override val brevkode: String? = "NAV 15-08.01"
    override val dokumentKategori: String = "ES" // ES Elektronisk skjema  - https://confluence.adeo.no/display/BOA/Dokumentkategori

}