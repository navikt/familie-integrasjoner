package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import org.springframework.stereotype.Component

@Component
internal class KodeverkHelsesjekk(kodeverkClient: KodeverkClient) : AbstractHealthIndicator(kodeverkClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.kodeverk", "status", "nede")
}
