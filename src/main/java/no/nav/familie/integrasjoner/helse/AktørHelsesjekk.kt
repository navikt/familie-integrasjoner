package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.AktørregisterClient
import org.springframework.stereotype.Component

@Component
internal class AktørHelsesjekk(aktørregisterClient: AktørregisterClient) : AbstractHealthIndicator(aktørregisterClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.aktoer", "status", "nede")
}