package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

abstract class KlageVedtak(
    override val fagsakSystem: Fagsystem,
    final override val tema: Tema,
    final override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
) : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val kanal: String? = null
    override val tittel: String? = null
    override val brevkode: String = "KLAGE_VEDTAKSBREV_${behandlingstema.name.uppercase()}"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB
}

@Component
object KlageVedtakOvergangsstønad : KlageVedtak(
    fagsakSystem = Fagsystem.EF,
    tema = Tema.ENF,
    behandlingstema = Behandlingstema.Overgangsstønad,
    dokumenttype = Dokumenttype.KLAGE_VEDTAKSBREV_OVERGANGSSTØNAD
)

@Component
object KlageVedtakBarnetilsyn : KlageVedtak(
    fagsakSystem = Fagsystem.EF,
    tema = Tema.ENF,
    behandlingstema = Behandlingstema.Barnetilsyn,
    dokumenttype = Dokumenttype.KLAGE_VEDTAKSBREV_BARNETILSYN
)

@Component
object KlageVedtakSkolepenger : KlageVedtak(
    fagsakSystem = Fagsystem.EF,
    tema = Tema.ENF,
    behandlingstema = Behandlingstema.Skolepenger,
    dokumenttype = Dokumenttype.KLAGE_VEDTAKSBREV_SKOLEPENGER
)

@Component
object KlageVedtakBarnetrygd : KlageVedtak(
    fagsakSystem = Fagsystem.BA,
    tema = Tema.BAR,
    behandlingstema = Behandlingstema.Barnetrygd,
    dokumenttype = Dokumenttype.KLAGE_VEDTAKSBREV_BARNETRYGD
)

@Component
object KlageVedtakKontantstøtte : KlageVedtak(
    fagsakSystem = Fagsystem.KONT,
    tema = Tema.KON,
    behandlingstema = Behandlingstema.Kontantstøtte,
    dokumenttype = Dokumenttype.KLAGE_VEDTAKSBREV_KONTANTSTØTTE
)
