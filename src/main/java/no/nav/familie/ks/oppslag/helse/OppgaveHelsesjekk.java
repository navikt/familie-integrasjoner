package no.nav.familie.ks.oppslag.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.ks.oppslag.oppgave.internal.OppgaveClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class OppgaveHelsesjekk implements HealthIndicator {

    private final Counter oppgaveNede = Metrics.counter("helsesjekk.oppgave", "status", "nede");
    private OppgaveClient oppgaveClient;

    public OppgaveHelsesjekk(@Autowired OppgaveClient oppgaveClient) {
        this.oppgaveClient = oppgaveClient;
    }

    @Override
    public Health health() {
        try {
            oppgaveClient.ping();
            return Health.up().build();
        } catch (Exception e) {
            oppgaveNede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", e.getMessage()).build();
        }
    }
}
