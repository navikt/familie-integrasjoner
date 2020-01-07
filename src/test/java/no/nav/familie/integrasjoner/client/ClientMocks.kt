package no.nav.familie.integrasjoner.client

import io.mockk.mockk
import no.nav.familie.integrasjoner.client.rest.*
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import no.nav.familie.integrasjoner.client.soap.InnsynJournalSoapClient
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component

@Component
class ClientMocks {

    @Bean
    @Primary
    fun mockAktørregisterRestClient(): AktørregisterRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockAzureGraphRestClient(): AzureGraphRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockDokarkivRestClient(): DokarkivRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockInfotrygdRestClient(): InfotrygdRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockMedlRestClient(): MedlRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockOppgaveRestClient(): OppgaveRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockSafRestClient(): SafRestClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockEgenAnsattSoapClient(): EgenAnsattSoapClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockInnsynJournalSoapClient(): InnsynJournalSoapClient {
        return mockk()
    }

    @Bean
    @Primary
    fun mockPersonSoapClient(): PersonSoapClient {
        return mockk()
    }
}