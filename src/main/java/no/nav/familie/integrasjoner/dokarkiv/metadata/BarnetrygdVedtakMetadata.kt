package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.oppgave.Behandlingstema
import org.springframework.stereotype.Component

@Component
object BarnetrygdVedtakMetadata : DokumentMetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: String? = "BA"
    override val tema: String = "BAR"
    override val behandlingstema: String? = Behandlingstema.Barnetrygd.value // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = ""
    override val dokumentTypeId: String = "BARNETRYGD_VEDTAK"
    override val tittel: String? = "Vedtak om innvilgelse av barnetrygd"
    override val brevkode: String? = "innvilget"
    override val dokumentKategori: String = "VB"

}