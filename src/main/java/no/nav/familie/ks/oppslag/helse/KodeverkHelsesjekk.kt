package no.nav.familie.ks.oppslag.helse

import io.micrometer.core.instrument.Metrics
import no.nav.familie.ef.mottak.api.kodeverk.KodeverkClient
import org.springframework.boot.actuate.health.Health
import org.springframework.boot.actuate.health.HealthIndicator

class KodeverkHelsesjekk(val kodeverkClient: KodeverkClient) : HealthIndicator {
    private val kodeverkNede = Metrics.counter("helsesjekk.kodeverk", "status", "nede")

    override fun health(): Health {
        return try {
            kodeverkClient.ping()
            Health.up().build()
        } catch (e: Exception) {
            kodeverkNede.increment()
            Health.status("DOWN-NONCRITICAL").withDetail("Feilmelding", e.message).build()
        }
    }

}
