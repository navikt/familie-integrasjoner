package no.nav.familie.integrasjoner.oppgave

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.*
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
    @Primary fun oppgaveMockClient(): OppgaveRestClient {
        val klient = mock(OppgaveRestClient::class.java)
        `when`(klient.finnOppgave(ArgumentMatchers.any(
                Oppgave::class.java))).thenReturn(OppgaveJsonDto(id = 42))
        `when`(klient.finnOppgave(ArgumentMatchers.anyString()))
                .thenReturn(OppgaveJsonDto(id = 42))
        `when`(klient.finnOppgave(matcherBeskrivelse("test RestClientException")))
                .thenThrow(HttpClientErrorException.create(HttpStatus.ACCEPTED,
                                                           "status text",
                                                           HttpHeaders(),
                                                           null,
                                                           null))
        `when`(klient.finnOppgave(matcherBeskrivelse("test oppgave ikke funnet")))
                .thenThrow(OppgaveIkkeFunnetException("Mislykket finnOppgave request med url: ..."))
        `when`(klient.finnOppgave(matcherBeskrivelse("test generell feil")))
                .thenThrow(RuntimeException("Uventet feil"))
        doNothing().`when`(klient)
                .oppdaterOppgave(ArgumentMatchers.any(), ArgumentMatchers.anyString())
        doNothing().`when`(klient).ping()
        return klient
    }

    private fun matcherBeskrivelse(beskrivelse: String): Oppgave {
        return ArgumentMatchers.eq(Oppgave("1234567891011",
                                           "1",
                                           null,
                                           beskrivelse))
    }
}