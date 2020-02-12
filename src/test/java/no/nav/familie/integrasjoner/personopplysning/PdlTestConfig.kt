package no.nav.familie.integrasjoner.personopplysning

import io.mockk.every
import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsDato
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class PdlTestConfig {

    @Bean
    @Profile("mock-pdl")
    @Primary
    fun pdlRestClientMock(): PdlRestClient {
        val klient = mockk<PdlRestClient>(relaxed = true)

        every {
            klient.hentFødselsdato(any(), any())
        } returns PdlFødselsDato("1980-05-12")
        return klient
    }
}