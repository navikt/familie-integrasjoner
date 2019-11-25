package no.nav.familie.ks.oppslag.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.ks.oppslag.egenansatt.internal.EgenAnsattConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.NestedExceptionUtils;

public class EgenAnsattV1Helsesjekk implements HealthIndicator {

    private EgenAnsattConsumer egenAnsattV1;
    private final Counter egenAnsattV1Nede = Metrics.counter("helsesjekk.egenAnsattV1", "status", "nede");

    public EgenAnsattV1Helsesjekk(@Autowired EgenAnsattConsumer egenAnsattV1) {
        this.egenAnsattV1 = egenAnsattV1;
    }

    @Override
    public Health health() {
        try {
            egenAnsattV1.ping();
            return Health.up().build();
        } catch (Exception e) {
            egenAnsattV1Nede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).getClass().getName() + ": " + e.getMessage()).build();
        }
    }
}
