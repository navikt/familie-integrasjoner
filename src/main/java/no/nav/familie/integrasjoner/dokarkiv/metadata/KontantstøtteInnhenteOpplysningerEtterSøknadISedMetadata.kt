package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object KontantstøtteInnhenteOpplysningerEtterSøknadISedMetadata : Dokumentmetadata {
    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.KONT
    override val tema: Tema = Tema.KON
    override val behandlingstema: Behandlingstema? = null
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.KONTANTSTØTTE_INNHENTE_OPPLYSNINGER_ETTER_SØKNAD_I_SED
    override val tittel: String = "Innhente opplysninger etter søknad i SED"
    override val brevkode: String = "innhente-opplysninger-etter-søknad-i-sed"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
