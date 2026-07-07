package no.nav.familie.integrasjoner.felles.config

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.felles.tokenklient.sts.StsRestClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class StsTestConfig {
    @Bean
    @Primary
    @Profile("mock-sts")
    fun stsRestClientMock(): StsRestClient {
        val client = mockk<StsRestClient>()
        every { client.systemOIDCToken } returns "MOCKED-STS-OIDC-TOKEN"
        return client
    }
}
