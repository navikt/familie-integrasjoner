package no.nav.familie.integrasjoner.arbeidoginntekt

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.ArbeidOgInntektClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class ArbeidOgInntektTestConfig {
    @Bean
    @Profile("mock-arbeid-og-inntekt")
    @Primary
    @Throws(Exception::class)
    fun clientMock(): ArbeidOgInntektClient {
        val clientMock: ArbeidOgInntektClient = mockk(relaxed = true)
        every { clientMock.hentUrlTilArbeidOgInntekt(any()) } returns "https://www.gyldig-url.no"
        return clientMock
    }
}
