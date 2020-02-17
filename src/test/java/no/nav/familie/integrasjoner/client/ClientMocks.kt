package no.nav.familie.integrasjoner.client

import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.InfotrygdRestClient
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class ClientMocks {

    @Bean
    @Primary
    @Profile("mock-infotrygd")
    fun mockInfotrygdRestClient(): InfotrygdRestClient {
        return mockk(relaxed = true)
    }

    @Bean
    @Primary
    @Profile("mock-egenansatt")
    fun mockEgenAnsattSoapClient(): EgenAnsattSoapClient {
        return mockk(relaxed = true)
    }
}