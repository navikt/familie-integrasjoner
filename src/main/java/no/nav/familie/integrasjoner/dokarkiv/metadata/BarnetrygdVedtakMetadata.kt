package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object BarnetrygdVedtakMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.BA
    override val tema: Tema = Tema.BAR
    override val behandlingstema: Behandlingstema = Behandlingstema.Barnetrygd
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETRYGD_VEDTAK
    override val tittel: String = "Vedtak om innvilgelse av barnetrygd"
    override val brevkode: String = "BAA1"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}
