package no.nav.familie.ks.oppslag.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.ks.oppslag.infotrygd.InfotrygdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.NestedExceptionUtils;

public class InfotrygdHelsesjekk implements HealthIndicator {

    private final Counter infotrygdNede = Metrics.counter("helsesjekk.infotrygd", "status", "nede");
    private InfotrygdService infotrygdService;

    public InfotrygdHelsesjekk(@Autowired InfotrygdService infotrygdService) {
        this.infotrygdService = infotrygdService;
    }

    @Override
    public Health health() {
        try {
            infotrygdService.ping();
            return Health.up().build();
        } catch(Exception e) {
            infotrygdNede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).getClass().getName() + ": " + e.getMessage()).build();
        }
    }
}
