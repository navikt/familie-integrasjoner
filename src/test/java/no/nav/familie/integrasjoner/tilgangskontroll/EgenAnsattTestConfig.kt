package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.mockk
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class EgenAnsattTestConfig {

    @Bean
    @Primary
    @Profile("mock-egenansatt")
    fun mockEgenAnsattSoapClient(): EgenAnsattSoapClient {
        return mockk(relaxed = true)
    }
}
