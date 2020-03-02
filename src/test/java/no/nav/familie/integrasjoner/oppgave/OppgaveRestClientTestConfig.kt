package no.nav.familie.integrasjoner.oppgave

import io.mockk.*
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class OppgaveRestClientTestConfig {

    @Bean
    @Profile("mock-oppgave")
    @Primary
    fun OppgaveMockRestClient(): OppgaveRestClient {
        val klient: OppgaveRestClient = mockk(relaxed = true)
        val response = OppgaveJsonDto(id = 42)

        every {
            klient.finnOppgave(any())
        } returns response

        every {
            klient.finnOppgaveMedId(any())
        } returns response

        every {
            klient.opprettOppgave(any())
        } returns 42

        return klient
    }
}