package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object BarnetrygdInformasjonsbrevTilForelderOmfattetNorskLovgivningVarselOmÅrligKontrollMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.BA
    override val tema: Tema = Tema.BAR
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETRYGD_INFORMASJONSBREV_TIL_FORELDER_OMFATTET_NORSK_LOVGIVNING_VARSEL_OM_ÅRLIG_KONTROLL
    override val tittel: String = "Informasjonsbrev om barnetrygd"
    override val brevkode: String = "informasjonsbrev-varsel-om-årlig-kontroll"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
