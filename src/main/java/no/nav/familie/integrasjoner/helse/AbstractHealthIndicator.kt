package no.nav.familie.integrasjoner.helse

import no.nav.familie.integrasjoner.felles.Pingable
import org.slf4j.LoggerFactory
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

/**
 * Local replacement for no.nav.familie.restklient.health.AbstractHealthIndicator.
 */
abstract class AbstractHealthIndicator(
    private val pingable: Pingable,
    private val name: String,
) : HealthIndicator {
    private val log = LoggerFactory.getLogger(this::class.java)

    override fun health(): Health =
        try {
            pingable.ping()
            Health.up().withDetail("name", name).build()
        } catch (e: Exception) {
            log.warn("Helsesjekk feilet for $name: ${e.message}")
            Health
                .down()
                .withDetail("name", name)
                .withException(e)
                .build()
        }
}
