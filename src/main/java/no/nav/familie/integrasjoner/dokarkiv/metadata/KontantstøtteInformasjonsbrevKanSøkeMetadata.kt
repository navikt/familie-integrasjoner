package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontantstøtteInformasjonsbrevKanSøkeMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.KS
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_INFORMASJONSBREV_KAN_SØKE
    override val tittel: String = "Informasjonsbrev kan søke - kontantstøtte"
    override val brevkode: String = "informasjonsbrev-kan-soke"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}