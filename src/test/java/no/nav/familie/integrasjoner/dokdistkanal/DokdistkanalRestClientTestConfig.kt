package no.nav.familie.integrasjoner.dokdistkanal

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.DokdistkanalRestClient
import no.nav.familie.integrasjoner.dokdistkanal.domene.BestemDistribusjonskanalResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class DokdistkanalRestClientTestConfig {
    @Bean
    @Profile("mock-dokdistkanal")
    @Primary
    fun dokdistkanalRestClient(): DokdistkanalRestClient {
        val klient: DokdistkanalRestClient = mockk(relaxed = true)
        val response =
            BestemDistribusjonskanalResponse(
                distribusjonskanal = "PRINT",
                regel = "REGEL",
                regelBegrunnelse = "regelbegrunnelse",
            )

        every {
            klient.bestemDistribusjonskanal(any())
        } returns response

        return klient
    }
}
