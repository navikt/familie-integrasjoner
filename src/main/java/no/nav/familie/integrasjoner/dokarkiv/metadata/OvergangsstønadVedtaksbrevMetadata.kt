package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.oppgave.Behandlingstema
import org.springframework.stereotype.Component

@Component
object OvergangsstønadVedtaksbrevMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "EF"
    override val tema: String = "ENF"
    override val behandlingstema: String? = Behandlingstema.Overgangsstønad.value
    override val kanal: String? = null
    override val dokumentTypeId: String = "VEDTAKSBREV_OVERGANGSSTØNAD"
    override val tittel: String? = "Vedtak om innvilgelse av overgangsstønad"
    override val brevkode: String? = "EFA1"
    override val dokumentKategori: String = "VB"
}

