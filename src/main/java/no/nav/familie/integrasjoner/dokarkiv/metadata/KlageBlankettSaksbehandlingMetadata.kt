package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

sealed class KlageBlankettSaksbehandling(
    override val fagsakSystem: Fagsystem,
    final override val tema: Tema,
    final override val behandlingstema: Behandlingstema,
    override val dokumenttype: Dokumenttype,
) : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.NOTAT
    override val kanal: String? = null
    override val tittel: String? = null
    override val brevkode: String = "KLAGE_BLANKETT_SAKSBEHANDLING_${behandlingstema.name.uppercase()}"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.IS
}

@Component
object KlageBlankettSaksbehandlingOvergangsstønad : KlageBlankettSaksbehandling(
    fagsakSystem = Fagsystem.EF,
    tema = Tema.ENF,
    behandlingstema = Behandlingstema.Overgangsstønad,
    dokumenttype = Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_OVERGANGSSTØNAD,
)

@Component
object KlageBlankettSaksbehandlingBarnetilsyn : KlageBlankettSaksbehandling(
    fagsakSystem = Fagsystem.EF,
    tema = Tema.ENF,
    behandlingstema = Behandlingstema.Barnetilsyn,
    dokumenttype = Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_BARNETILSYN,
)

@Component
object KlageBlankettSaksbehandlingSkolepenger : KlageBlankettSaksbehandling(
    fagsakSystem = Fagsystem.EF,
    tema = Tema.ENF,
    behandlingstema = Behandlingstema.Skolepenger,
    dokumenttype = Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_SKOLEPENGER,
)

@Component
object KlageBlankettSaksbehandlingBarnetrygd : KlageBlankettSaksbehandling(
    fagsakSystem = Fagsystem.BA,
    tema = Tema.BAR,
    behandlingstema = Behandlingstema.Barnetrygd,
    dokumenttype = Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_BARNETRYGD,
)

@Component
object KlageBlankettSaksbehandlingKontantstøtte : KlageBlankettSaksbehandling(
    fagsakSystem = Fagsystem.KONT,
    tema = Tema.KON,
    behandlingstema = Behandlingstema.Kontantstøtte,
    dokumenttype = Dokumenttype.KLAGE_BLANKETT_SAKSBEHANDLING_KONTANTSTØTTE,
)
