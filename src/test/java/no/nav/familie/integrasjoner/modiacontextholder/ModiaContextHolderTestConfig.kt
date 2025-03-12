package no.nav.familie.integrasjoner.modiacontextholder

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.ModiaContextHolderClient
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderResponse
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class ModiaContextHolderTestConfig {
    @Bean
    @Profile("mock-modia-context-holder")
    @Primary
    @Throws(Exception::class)
    fun clientMock(): ModiaContextHolderClient {
        val clientMock: ModiaContextHolderClient = mockk(relaxed = true)
        every { clientMock.hentContext() } returns modiaResponse()
        every { clientMock.settContext(any()) } returns modiaResponse()
        return clientMock
    }

    private fun modiaResponse(): ModiaContextHolderResponse =
        ModiaContextHolderResponse(
            aktivEnhet = "0300",
            aktivBruker = "13025514402",
        )
}
