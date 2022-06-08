package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object OvergangsstønadBlankettMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.NOTAT
    override val fagsakSystem: Fagsystem = Fagsystem.IT01
    override val tema: Tema = Tema.ENF
    override val behandlingstema: Behandlingstema = Behandlingstema.Overgangsstønad
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.OVERGANGSSTØNAD_BLANKETT
    override val tittel: String = "Blankett for overgangsstønad - enslig mor eller far"
    override val brevkode: String? = null
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.IS
}
