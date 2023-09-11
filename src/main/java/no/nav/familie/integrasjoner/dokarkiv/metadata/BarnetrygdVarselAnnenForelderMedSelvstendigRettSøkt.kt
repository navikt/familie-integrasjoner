package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object BarnetrygdVarselAnnenForelderMedSelvstendigRettSøkt : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.BA
    override val tema: Tema = Tema.BAR
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.BARNETRYGD_VARSEL_ANNEN_FORELDER_MED_SELVSTENDIG_RETT_SØKT
    override val tittel: String = "Varsel annen forelder med selvstendig rett søkt"
    override val brevkode: String = "varsel-annen-forelder-med-selvstendig-rett-søkt"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
