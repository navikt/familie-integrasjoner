package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.kontrakter.felles.journalpost.*
import org.mockito.ArgumentCaptor
import org.mockito.Mockito
import org.mockito.invocation.InvocationOnMock
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Configuration
class HentJournalpostTestConfig {

    @Bean
    @Profile("mock-saf")
    @Primary fun safRestClientMock(): SafRestClient {
        val klient = Mockito.mock(SafRestClient::class.java)
        val stringCaptor =
                ArgumentCaptor.forClass(String::class.java)
        Mockito.`when`(klient.hentJournalpost(stringCaptor.capture()))
                .thenAnswer {
                    Journalpost(
                            journalpostId = stringCaptor.value,
                            journalposttype = Journalposttype.I,
                            journalstatus = Journalstatus.JOURNALFOERT,
                            tema = "BAR",
                            behandlingstema = null,
                            sak = Sak("1111" + stringCaptor.value,
                                      "GSAK",
                                      null,
                                      null, null), bruker = Bruker("1234567890123", BrukerIdType.AKTOERID),
                            journalforendeEnhet =  "9999",
                            kanal = "EIA",
                            dokumenter = emptyList())
                }

        Mockito.doNothing().`when`(klient).ping()
        return klient
    }
}