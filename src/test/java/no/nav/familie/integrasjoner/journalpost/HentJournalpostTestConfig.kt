package no.nav.familie.integrasjoner.journalpost

import io.mockk.*
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.kontrakter.felles.journalpost.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class HentJournalpostTestConfig {

    @Bean
    @Profile("mock-saf")
    @Primary fun safRestClientMock(): SafRestClient {
        val klient: SafRestClient = mockk(relaxed = true)
        val slot = slot<String>()

        every { klient.hentJournalpost(capture(slot)) } answers {
            Journalpost(
                    journalpostId = slot.captured,
                    journalposttype = Journalposttype.I,
                    journalstatus = Journalstatus.JOURNALFOERT,
                    tema = "BAR",
                    behandlingstema = null,
                    sak = Sak("1111" + slot.captured,
                              "GSAK",
                              null,
                              null, null),
                    bruker = Bruker("1234567890123", BrukerIdType.AKTOERID),
                    journalforendeEnhet = "9999",
                    kanal = "EIA",
                    dokumenter = emptyList())
        }

        return klient
    }
}
