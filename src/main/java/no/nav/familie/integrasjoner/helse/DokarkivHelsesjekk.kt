package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import org.springframework.stereotype.Component

@Component
internal class DokarkivHelsesjekk(dokarkivRestClient: DokarkivRestClient) : AbstractHealthIndicator(dokarkivRestClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.dokarkiv", "status", "nede")
}