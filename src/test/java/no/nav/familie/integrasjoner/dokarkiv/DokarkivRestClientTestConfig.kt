package no.nav.familie.integrasjoner.dokarkiv

import io.mockk.*
import no.nav.familie.integrasjoner.client.rest.DokarkivLogiskVedleggRestClient
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class DokarkivRestClientTestConfig {

    @Bean
    @Profile("mock-dokarkiv")
    @Primary
    fun dokarkivMockRestClient(): DokarkivRestClient {
        val klient: DokarkivRestClient = mockk(relaxed = true)
        val pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val response = OpprettJournalpostResponse(journalpostId = LocalDateTime.now().format(pattern),
                                                  journalpostferdigstilt = false)
        every {
            klient.lagJournalpost(any(), any())
        } returns response

        every {
            klient.ferdigstillJournalpost(any(), any())
        } just Runs

        return klient
    }

    @Bean
    @Profile("mock-dokarkiv")
    @Primary
    fun dokarkivLogiskVedleggMockRestClient(): DokarkivLogiskVedleggRestClient {
        val klient: DokarkivLogiskVedleggRestClient = mockk(relaxed = true)

        every {
            klient.opprettLogiskVedlegg(any(), any())
        } returns DokarkivController.LogiskVedleggResponse(123456789L)

        every {
            klient.slettLogiskVedlegg(any(), any())
        } just Runs

        return klient
    }
}