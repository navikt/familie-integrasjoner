package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontantstøtteSøknadEØSMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: Fagsystem? = null
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema = Behandlingstema.KontantstøtteEØS
    override val kanal: String = "NAV_NO"
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_EØS
    override val tittel: String =
        "Tilleggsskjema ved krav om utbetaling av barnetrygd " +
            "og/eller kontantstøtte på grunnlag av regler om eksport etter EØS-avtalen"
    override val brevkode: String = "NAV 34-00.15"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.SOK
}
