package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object OvergangsstønadTilbakebetalingVedtakMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.EF
    override val tema: Tema = Tema.ENF
    override val behandlingstema: Behandlingstema =            Behandlingstema.Tilbakebetaling
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.OVERGANGSSTØNAD_TILBAKEKREVING_VEDTAK
    override val tittel: String? = null
    override val brevkode: String = Fagsystem.EF.name + "-TILB"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.VB

}
