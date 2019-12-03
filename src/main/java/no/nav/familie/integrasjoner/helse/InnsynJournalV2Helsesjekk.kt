package no.nav.familie.integrasjoner.helse;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import no.nav.familie.integrasjoner.journalpost.internal.InnsynJournalConsumer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.core.NestedExceptionUtils;

public class InnsynJournalV2Helsesjekk implements HealthIndicator {

    private final Counter innsynJournalV2Nede = Metrics.counter("helsesjekk.innsynJournalV2", "status", "nede");
    private InnsynJournalConsumer innsynJournalV2;

    public InnsynJournalV2Helsesjekk(@Autowired InnsynJournalConsumer innsynJournalV2) {
        this.innsynJournalV2 = innsynJournalV2;
    }

    @Override
    public Health health() {
        try {
            innsynJournalV2.ping();
            return Health.up().build();
        } catch (Exception e) {
            innsynJournalV2Nede.increment();
            return Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).getClass().getName() + ": " + e.getMessage()).build();
        }
    }
}
