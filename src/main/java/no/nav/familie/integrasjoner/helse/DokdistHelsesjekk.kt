package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import org.springframework.stereotype.Component

@Component
internal class DokdistHelsesjekk(dokdistRestClient: DokdistRestClient) : AbstractHealthIndicator(dokdistRestClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.dokdist", "status", "nede")
}