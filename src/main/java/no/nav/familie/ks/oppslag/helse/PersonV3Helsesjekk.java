package no.nav.familie.ks.oppslag.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.ks.oppslag.personopplysning.internal.PersonConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;

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
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", e.getMessage()).build();
        }
    }
}
