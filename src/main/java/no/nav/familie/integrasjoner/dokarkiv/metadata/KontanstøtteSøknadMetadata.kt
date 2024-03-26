package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontanstøtteSøknadMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: Fagsystem? = null
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema = Behandlingstema.Kontantstøtte
    override val kanal: String = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_SØKNAD
    override val tittel: String = "Søknad om kontantstøtte til småbarnsforeldre"
    override val brevkode: String = "NAV 34-00.08"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.SOK
}
