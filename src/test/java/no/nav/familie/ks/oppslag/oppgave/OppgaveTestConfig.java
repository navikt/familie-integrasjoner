package no.nav.familie.ks.oppslag.oppgave;

import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.oppslag.oppgave.internal.OppgaveConsumer;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSOppgaveIkkeFunnetException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSOptimistiskLasingException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.WSOpprettOppgaveResponse;
import org.jetbrains.annotations.Nullable;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.AdditionalMatchers.not;
import static org.mockito.AdditionalMatchers.and;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Configuration
public class OppgaveTestConfig {

    @Bean
    @Profile("mock-oppgave")
    @Primary
    public OppgaveConsumer oppgaveConsumerMock() throws WSSikkerhetsbegrensningException, WSOppgaveIkkeFunnetException, WSOptimistiskLasingException {
        OppgaveConsumer oppgaveConsumer = mock(OppgaveConsumer.class);

        when(oppgaveConsumer.opprettOppgave(matcherBeskrivelse("Test null-response"))).thenReturn(new WSOpprettOppgaveResponse());
        when(oppgaveConsumer.opprettOppgave(matcherBeskrivelse("WS-exception"))).thenThrow(new WSSikkerhetsbegrensningException("feilmelding"));
        when(oppgaveConsumer.oppdaterOppgave(matcherBeskrivelse("WS-exception"))).thenThrow(new WSOppgaveIkkeFunnetException("feilmelding"));

        WSOpprettOppgaveResponse response = new WSOpprettOppgaveResponse();
        response.setOppgaveId("123");

        when(oppgaveConsumer.opprettOppgave(erIkkeEnTestCase())).thenReturn(response);
        when(oppgaveConsumer.oppdaterOppgave(erIkkeEnTestCase())).thenReturn(true);

        return oppgaveConsumer;
    }

    @Nullable
    private Oppgave erIkkeEnTestCase() {
        return and(not(matcherBeskrivelse("Test null-response")), not(matcherBeskrivelse("WS-exception")));
    }

    private Oppgave matcherBeskrivelse(String beskrivelse) {
        return eq(new Oppgave("", "", null, "", beskrivelse, 0));
    }
}
