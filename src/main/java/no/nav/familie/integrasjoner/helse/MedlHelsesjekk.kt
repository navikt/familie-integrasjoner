package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.MedlRestClient
import org.springframework.stereotype.Component

@Component
internal class MedlHelsesjekk(medlRestClient: MedlRestClient) : AbstractHealthIndicator(medlRestClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.medl", "status", "nede")
}