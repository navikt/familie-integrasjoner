package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.SafKlient
import org.springframework.stereotype.Component

@Component
internal class SafHelsesjekk(safKlient: SafKlient) : AbstractHealthIndicator(safKlient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.saf", "status", "nede")
}