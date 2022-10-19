package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontantstøtteVedtakMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.KS
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema = Behandlingstema.Kontantstøtte
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_VEDTAK
    override val tittel: String = "Vedtak om innvilgelse av kontantstøtte"
    override val brevkode: String = "KS04"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}
