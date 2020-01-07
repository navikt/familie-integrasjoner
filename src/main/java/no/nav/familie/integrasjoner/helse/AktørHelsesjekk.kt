package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient
import org.springframework.stereotype.Component

@Component
internal class AktørHelsesjekk(aktørregisterRestClient: AktørregisterRestClient)
    : AbstractHealthIndicator(aktørregisterRestClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.aktoer", "status", "nede")
}