package no.nav.familie.integrasjoner.egenansatt

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
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
        val egenAnsattClient: EgenAnsattRestClient = mockk()
        every { egenAnsattClient.erEgenAnsatt(any()) } returns true
        return egenAnsattClient
    }
}
