package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontantstøtteTilbakebetalingBrevMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.KS
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema = Behandlingstema.Tilbakebetaling
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_TILBAKEKREVING_BREV
    override val tittel: String? = null
    override val brevkode: String = Fagsystem.KS.name + "-TILB"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
