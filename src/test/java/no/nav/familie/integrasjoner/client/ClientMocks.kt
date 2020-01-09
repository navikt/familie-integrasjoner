package no.nav.familie.integrasjoner.client

import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.*
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import no.nav.familie.integrasjoner.client.soap.InnsynJournalSoapClient
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class ClientMocks {

    @Bean
    @Primary
    @Profile("mock-aktor")
    fun mockAktørregisterRestClient(): AktørregisterRestClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-infotrygd")
    fun mockInfotrygdRestClient(): InfotrygdRestClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-medlemskap")
    fun mockMedlRestClient(): MedlRestClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-oppgave")
    fun mockOppgaveRestClient(): OppgaveRestClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-saf")
    fun mockSafRestClient(): SafRestClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-egenansatt")
    fun mockEgenAnsattSoapClient(): EgenAnsattSoapClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-innsyn")
    fun mockInnsynJournalSoapClient(): InnsynJournalSoapClient {
        return mockk()
    }

    @Bean
    @Primary
    @Profile("mock-personopplysninger")
    fun mockPersonSoapClient(): PersonSoapClient {
        return mockk()
    }
}