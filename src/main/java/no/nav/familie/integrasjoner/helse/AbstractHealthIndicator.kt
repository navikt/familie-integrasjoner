package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import no.nav.familie.integrasjoner.client.Pingable
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.core.NestedExceptionUtils

internal abstract class AbstractHealthIndicator(private val pingable: Pingable) : HealthIndicator {

    protected abstract val failureCounter: Counter

    override fun health(): Health {
        return try {
            pingable.ping()
            Health.up().build()
        } catch (e: Exception) {
            failureCounter.increment()
            Health.status("DOWN-NONCRITICAL")
                    .withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).javaClass.name + ": " + e.message)
                    .build()
        }
    }

}
