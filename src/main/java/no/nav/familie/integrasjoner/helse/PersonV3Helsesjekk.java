package no.nav.familie.integrasjoner.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.integrasjoner.personopplysning.internal.PersonConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.NestedExceptionUtils;

public class PersonV3Helsesjekk implements HealthIndicator {

    private PersonConsumer personV3;

    private final Counter personV3Nede = Metrics.counter("helsesjekk.personV3", "status", "nede");

    public PersonV3Helsesjekk(@Autowired PersonConsumer personConsumer) {
        this.personV3 = personConsumer;
    }

    @Override
    public Health health() {
        try {
            personV3.ping();
            return Health.up().build();
        } catch(Exception e) {
            personV3Nede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).getClass().getName() + ": " + e.getMessage()).build();
        }
    }
}
