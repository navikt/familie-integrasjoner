package no.nav.familie.integrasjoner.tilgangskontroll

import io.mockk.mockk
import no.nav.familie.integrasjoner.egenansatt.internal.EgenAnsattConsumer
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class EgenAnsattTestConfig {

    @Bean
    @Profile("mock-egenansatt")
    @Primary
    fun egenAnsattConsumerMock(): EgenAnsattConsumer {
        return mockk(relaxed = true)
    }
}
