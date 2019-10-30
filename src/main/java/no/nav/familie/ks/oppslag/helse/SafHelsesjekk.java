package no.nav.familie.ks.oppslag.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.ks.oppslag.journalpost.internal.SafKlient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

public class SafHelsesjekk implements HealthIndicator {

    private final Counter safNede = Metrics.counter("helsesjekk.saf", "status", "nede");

    private SafKlient safKlient;

    public SafHelsesjekk(@Autowired SafKlient safKlient) {
        this.safKlient = safKlient;
    }

    @Override
    public Health health() {
        try {
            safKlient.ping();
            return Health.up().build();
        } catch (Exception e) {
            safNede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", e.getMessage()).build();
        }
    }
}
