package no.nav.familie.integrasjoner.dokarkiv.metadata

import no.nav.familie.integrasjoner.dokarkiv.client.domene.JournalpostType
import org.springframework.stereotype.Component

@Component
object BarnetilsynSøknadMetadata : DokumentMetadata {
    override val journalpostType: JournalpostType = JournalpostType.INNGAAENDE
    override val fagsakSystem: String? = null
    override val tema: String = "ENF"
    override val behandlingstema: String? = "ab0028" // https://confluence.adeo.no/display/BOA/Behandlingstema
    override val kanal: String? = "NAV_NO"
    override val dokumentTypeId: String = "BARNETILSYNSTØNAD"
    override val tittel: String? = "Søknad om stønad til barnetilsyn - enslig mor eller far i arbeid"
    override val brevkode: String? = "NAV 15-00.02"
    override val dokumentKategori: String = "SOK"

}

// Mulige behandligstema:
//ab0216	Barnetilsyn - barn under 10 år
//ab0224	Gjenlevende - stønad til barnetilsyn
//ab0028    Barnetilsyn
//ab0086	Barnetilsyn - barn over 10 år
//ab0145	Barnetilsyn - familiemedlemmer