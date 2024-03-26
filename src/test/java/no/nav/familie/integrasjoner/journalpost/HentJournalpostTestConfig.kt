package no.nav.familie.integrasjoner.journalpost

import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.kontrakter.felles.BrukerIdType
import no.nav.familie.kontrakter.felles.journalpost.Bruker
import no.nav.familie.kontrakter.felles.journalpost.DokumentInfo
import no.nav.familie.kontrakter.felles.journalpost.Dokumentstatus
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.Journalposttype
import no.nav.familie.kontrakter.felles.journalpost.Journalstatus
import no.nav.familie.kontrakter.felles.journalpost.LogiskVedlegg
import no.nav.familie.kontrakter.felles.journalpost.Sak
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class HentJournalpostTestConfig {
    @Bean
    @Profile("mock-saf")
    @Primary
    fun safRestClientMock(): SafRestClient {
        val klient: SafRestClient = mockk(relaxed = true)
        val slot = slot<String>()

        every { klient.hentJournalpost(capture(slot)) } answers {
            Journalpost(
                journalpostId = slot.captured,
                journalposttype = Journalposttype.I,
                journalstatus = Journalstatus.MOTTATT,
                tema = "BAR",
                tittel = "Ent tittel",
                behandlingstema = null,
                sak =
                    Sak(
                        "1111" + slot.captured,
                        "GSAK",
                        null,
                        null,
                        null,
                    ),
                bruker = Bruker("1234567890123", BrukerIdType.AKTOERID),
                journalforendeEnhet = "9999",
                kanal = "EIA",
                dokumenter =
                    listOf(
                        DokumentInfo(
                            dokumentInfoId = "1234",
                            tittel = "Søknad om ytelse",
                            dokumentstatus = Dokumentstatus.FERDIGSTILT,
                            dokumentvarianter = emptyList(),
                            logiskeVedlegg =
                                listOf(
                                    LogiskVedlegg(
                                        logiskVedleggId = "0987",
                                        tittel = "Oppholdstillatelse",
                                    ),
                                ),
                        ),
                    ),
                relevanteDatoer = emptyList(),
            )
        }

        return klient
    }
}
