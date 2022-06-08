package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object BarnetilsynEttersendingMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: Fagsystem? = null
    override val tema: Tema = Tema.ENF
    override val behandlingstema: Behandlingstema = Behandlingstema.Barnetilsyn
    override val kanal: String = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETILSYNSTØNAD_ETTERSENDING
    override val tittel: String = "Ettersending barnetilsyn - enslig mor eller far i arbeid"
    override val brevkode: String = "NAVe 15-00.02"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.IS
}
