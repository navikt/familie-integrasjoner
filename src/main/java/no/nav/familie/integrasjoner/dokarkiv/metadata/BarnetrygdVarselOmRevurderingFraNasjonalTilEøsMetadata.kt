package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object BarnetrygdVarselOmRevurderingFraNasjonalTilEøsMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.BA
    override val tema: Tema = Tema.BAR
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETRYGD_VARSEL_OM_REVURDERING_FRA_NASJONAL_TIL_EØS
    override val tittel: String = "Varsler om revurdering fra nasjonal til EØS"
    override val brevkode: String = "varsler-om-revurdering-fra-nasjonal-til-eøs"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
