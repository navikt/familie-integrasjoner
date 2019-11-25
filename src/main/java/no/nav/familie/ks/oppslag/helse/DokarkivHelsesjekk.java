package no.nav.familie.ks.oppslag.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.ks.oppslag.dokarkiv.client.DokarkivClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.NestedExceptionUtils;

public class DokarkivHelsesjekk implements HealthIndicator {

    private final Counter dokarkivNede = Metrics.counter("helsesjekk.dokarkiv", "status", "nede");
    private DokarkivClient dokarkivClient;

    public DokarkivHelsesjekk(@Autowired DokarkivClient dokarkivClient) {
        this.dokarkivClient = dokarkivClient;
    }

    @Override
    public Health health() {
        try {
            dokarkivClient.ping();
            return Health.up().build();
        } catch (Exception e) {
            dokarkivNede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).getClass().getName() + ": " + e.getMessage()).build();
        }
    }
}
