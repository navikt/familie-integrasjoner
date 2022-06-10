package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import no.nav.familie.kontrakter.felles.Behandlingstema
import no.nav.familie.kontrakter.felles.Fagsystem
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.dokarkiv.Dokumenttype
import org.springframework.stereotype.Component

@Component
object SkolepengerFrittståendeBrevMetadata : Dokumentmetadata {

    override val journalpostType: JournalpostType = JournalpostType.UTGAAENDE
    override val fagsakSystem: Fagsystem = Fagsystem.EF
    override val tema: Tema = Tema.ENF
    override val behandlingstema: Behandlingstema = Behandlingstema.Skolepenger
    override val kanal: String? = null
    override val dokumenttype: Dokumenttype = Dokumenttype.SKOLEPENGER_FRITTSTÅENDE_BREV
    override val tittel: String? = null
    override val brevkode: String = "EFC2"
    override val dokumentKategori: Dokumentkategori = Dokumentkategori.B
}
