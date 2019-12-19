package no.nav.familie.integrasjoner.dokarkiv

import no.nav.familie.integrasjoner.dokarkiv.client.DokarkivClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
class DokarkivClientTestConfig {

    @Bean
    @Profile("mock-dokarkiv")
    @Primary
    fun dokarkivMockClient(): DokarkivClient {
        val klient = Mockito.mock(DokarkivClient::class.java)
        val pattern = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val response = OpprettJournalpostResponse(journalpostId = LocalDateTime.now().format(pattern),
                                                  journalpostferdigstilt = false)
        Mockito.`when`(klient.lagJournalpost(ArgumentMatchers.any<OpprettJournalpostRequest>(),
                                             ArgumentMatchers.anyBoolean(),
                                             ArgumentMatchers.anyString())).thenReturn(response)
        Mockito.doNothing().`when`(klient).ping()
        return klient
    }
}