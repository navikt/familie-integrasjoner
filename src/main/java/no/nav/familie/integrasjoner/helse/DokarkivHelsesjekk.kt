package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.DokarkivClient
import org.springframework.stereotype.Component

@Component
internal class DokarkivHelsesjekk(dokarkivClient: DokarkivClient) : AbstractHealthIndicator(dokarkivClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.dokarkiv", "status", "nede")
}