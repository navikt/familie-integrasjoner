package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object OvergangsstønadFrittståendeBrevMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.EF
    override val tema: Tema = Tema.ENF
    override val behandlingstema: Behandlingstema = Behandlingstema.Overgangsstønad
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.OVERGANGSSTØNAD_FRITTSTÅENDE_BREV
    override val tittel: String = "Vedtak om innvilgelse av overgangsstønad"
    override val brevkode: String = "EFA1"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}

