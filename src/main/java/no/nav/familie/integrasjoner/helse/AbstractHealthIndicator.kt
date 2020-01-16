package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import no.nav.familie.integrasjoner.client.Pingable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator
import org.springframework.core.NestedExceptionUtils

internal abstract class AbstractHealthIndicator(private val pingable: Pingable) : HealthIndicator {

    protected val log: Logger = LoggerFactory.getLogger(this::class.java)
    protected abstract val failureCounter: Counter

    override fun health(): Health {
        return try {
            pingable.ping()
            Health.up().build()
        } catch (e: Exception) {
            failureCounter.increment()
            log.warn("Feil ved helsesjekk", e)
            Health.status("DOWN-NONCRITICAL")
                    .withDetail("Feilmelding", NestedExceptionUtils.getMostSpecificCause(e).javaClass.name + ": " + e.message)
                    .build()
        }
    }

}
