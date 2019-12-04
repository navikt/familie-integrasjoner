package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveClient
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import no.nav.familie.ks.kontrakter.oppgave.Oppgave
import org.mockito.ArgumentMatchers
import org.mockito.Mockito
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.web.client.HttpClientErrorException

@Configuration
class OppgaveTestConfig {
    @Bean
    @Profile("mock-oppgave")
    @Primary fun oppgaveMockClient(): OppgaveClient {
        val klient = Mockito.mock(OppgaveClient::class.java)
        Mockito.`when`(klient.finnOppgave(ArgumentMatchers.any(Oppgave::class.java)))
                .thenReturn(OppgaveJsonDto(1L))
        Mockito.`when`(klient.finnOppgave(matcherBeskrivelse("test RestClientException")))
                .thenThrow(HttpClientErrorException.create(HttpStatus.ACCEPTED,
                                                           "status text",
                                                           HttpHeaders(),
                                                           null,
                                                           null))
        Mockito.`when`(klient.finnOppgave(matcherBeskrivelse("test oppgave ikke funnet")))
                .thenThrow(OppgaveIkkeFunnetException("Mislykket finnOppgave request med url: ..."))
        Mockito.`when`(klient.finnOppgave(matcherBeskrivelse("test generell feil")))
                .thenThrow(RuntimeException("Uventet feil"))
        Mockito.doNothing().`when`(klient)
                .oppdaterOppgave(ArgumentMatchers.any(), ArgumentMatchers.anyString())
        Mockito.doNothing().`when`(klient).ping()
        return klient
    }

    private fun matcherBeskrivelse(beskrivelse: String): Oppgave {
        return ArgumentMatchers.eq(Oppgave("1234567891011",
                                           "1",
                                           null,
                                           beskrivelse))
    }
}