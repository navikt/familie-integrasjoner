package no.nav.familie.integrasjoner.egenansatt

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class EgenAnsattTestConfig {

    @Bean
    @Profile("mock-egenansatt")
    @Primary
    fun egenAnssattClientMock(): EgenAnsattRestClient {
        val egenAnsattClient: EgenAnsattRestClient = mockk(relaxed = true)
        every { egenAnsattClient.erEgenAnsatt(any<String>()) } returns true
        return egenAnsattClient
    }
}
