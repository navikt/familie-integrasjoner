package no.nav.familie.integrasjoner.sak

import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.SkyggesakRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class SkyggesakTestConfig {

    @Bean
    @Profile("mock-sak")
    @Primary
    fun SkyggesakMockRestClient(): SkyggesakRestClient {
        return mockk(relaxed = true)
    }


}