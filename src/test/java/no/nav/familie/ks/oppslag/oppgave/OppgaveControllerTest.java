package no.nav.familie.ks.oppslag.oppgave;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.oppslag.OppslagSpringRunnerTest;
import no.nav.familie.ks.oppslag.config.ApiExceptionHandler;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles(profiles = {"dev", "mock-oppgave", "mock-aktor"})
public class OppgaveControllerTest extends OppslagSpringRunnerTest {
    Logger oppgaveControllerLogger = (Logger) LoggerFactory.getLogger(OppgaveController.class);
    Logger exceptionHandler = (Logger) LoggerFactory.getLogger(ApiExceptionHandler.class);

    public static final String OPPDATER_OPPGAVE_URL = "/api/oppgave/oppdater";

    @Before
    public void setup() {
        oppgaveControllerLogger.addAppender(listAppender);
        exceptionHandler.addAppender(listAppender);

        headers.setBearerAuth(getLokalTestToken());
    }

    @Test
    public void skal_logge_stack_trace_ved_NullPointerException() {
        Oppgave test = new Oppgave("1234567891011", "1", null, "test NPE");

        restTemplate.exchange(
                localhost(OPPDATER_OPPGAVE_URL), HttpMethod.POST,new HttpEntity<>(test, headers), String.class
        );
        assertThat(loggingEvents).extracting(ILoggingEvent::getFormattedMessage).anyMatch(s -> s.contains("java.lang.NullPointerException\n\tat no.nav.familie.ks.oppslag.oppgave"));
    }

    @Test
    public void skal_skrive_til_logg_når_det_oppstår_RestClientException() {
        Oppgave test = new Oppgave("1234567891011", "1", null, "test RestClientException");

        restTemplate.exchange(
                localhost(OPPDATER_OPPGAVE_URL), HttpMethod.POST,new HttpEntity<>(test, headers), String.class
        );
        assertThat(loggingEvents).extracting(ILoggingEvent::getFormattedMessage).anyMatch(s -> s.contains("HttpClientErrorException"));
    }

    @Test
    public void skal_skrive_til_logg_hvis_oppgave_ikke_ble_funnet() {
        Oppgave test = new Oppgave("1234567891011", "1", null, "test oppgave ikke funnet");

        restTemplate.exchange(
                localhost(OPPDATER_OPPGAVE_URL), HttpMethod.POST,new HttpEntity<>(test, headers), String.class
        );
        assertThat(loggingEvents).extracting(ILoggingEvent::getFormattedMessage).anyMatch(s -> s.contains("OppgaveIkkeFunnetException"));
    }

    @Test
    public void skal_skrive_til_logg_ved_generell_feil() {
        Oppgave test = new Oppgave("1234567891011", "1", null, "test generell feil");

        restTemplate.exchange(
                localhost(OPPDATER_OPPGAVE_URL), HttpMethod.POST,new HttpEntity<>(test, headers), String.class
        );
        assertThat(loggingEvents).extracting(ILoggingEvent::getFormattedMessage).anyMatch(s -> s.contains("RuntimeException"));
    }
}
