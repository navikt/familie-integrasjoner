package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object ArbeidsregistreringsskjemaMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: Fagsystem? = null
    override val tema: Tema = Tema.ENF
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.SKJEMA_ARBEIDSSØKER
    override val tittel: String = "Enslig mor eller far som er arbeidssøker"
    override val brevkode: String = "NAV 15-08.01"

    // ES Elektronisk skjema  - https://confluence.adeo.no/display/BOA/Dokumentkategori
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.ES
}
