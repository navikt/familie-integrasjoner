package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.MedlClient
import org.springframework.stereotype.Component

@Component
internal class MedlHelsesjekk(medlClient: MedlClient) : AbstractHealthIndicator(medlClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.medl", "status", "nede")
}