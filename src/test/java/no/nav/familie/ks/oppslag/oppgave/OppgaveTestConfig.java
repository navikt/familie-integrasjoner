package no.nav.familie.ks.oppslag.oppgave;

import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.oppslag.oppgave.internal.OppgaveClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.doNothing;

@Configuration
public class OppgaveTestConfig {

    @Bean
    @Profile("mock-oppgave")
    @Primary
    public OppgaveClient oppgaveMockClient() {
        OppgaveClient klient = mock(OppgaveClient.class);

        when(klient.finnOppgave(matcherBeskrivelse("test RestClientException"))).thenThrow(HttpClientErrorException.create(HttpStatus.ACCEPTED, "status text", new HttpHeaders(), null, null));
        when(klient.finnOppgave(matcherBeskrivelse("test oppgave ikke funnet"))).thenThrow(new OppgaveIkkeFunnetException("Mislykket finnOppgave request med url: ..."));
        when(klient.finnOppgave(matcherBeskrivelse("test generell feil"))).thenThrow(new RuntimeException("Uventet feil"));

        doNothing().when(klient).oppdaterOppgave(any(), anyString());
        doNothing().when(klient).ping();
        return klient;
    }

    private Oppgave matcherBeskrivelse(String beskrivelse) {
        return eq(new Oppgave("1234567891011", "1", null, beskrivelse));
    }
}
