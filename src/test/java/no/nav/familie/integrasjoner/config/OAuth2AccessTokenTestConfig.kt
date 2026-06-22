package no.nav.familie.integrasjoner.config

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.entraid.EntraIDClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
@Profile("mock-oauth")
class OAuth2AccessTokenTestConfig {
    @Bean
    @Primary
    fun entraIDClientMock(): EntraIDClient {
        val mock = mockk<EntraIDClient>(relaxed = true)
        every { mock.hentMaskinTilMaskinToken(any()) } returns "mock-m2m-token"
        every { mock.hentOboToken(any(), any()) } returns "mock-obo-token"
        return mock
    }
}
