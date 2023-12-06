package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontantstøtteVarselAnnenForelderMedSelvstendigRettSøkt : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.KONT
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_VARSEL_ANNEN_FORELDER_MED_SELVSTENDIG_RETT_SØKT
    override val tittel: String = "Varsel om revurdering - kontantstøtte"
    override val brevkode: String = "varsel-annen-forelder-med-selvstendig-rett-søkt"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
