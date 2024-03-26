package no.nav.familie.integrasjoner.dokdist

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class DokdistRestClientTestConfig {
    @Bean
    @Profile("mock-dokdist")
    @Primary
    fun dokdistMockRestClient(): DokdistRestClient {
        val klient: DokdistRestClient = mockk(relaxed = true)
        val response = DistribuerJournalpostResponseTo("fd5a2ccb-a303-42fd-92fa-f5db70e7f324")

        every {
            klient.distribuerJournalpost(any())
        } returns response

        return klient
    }
}
