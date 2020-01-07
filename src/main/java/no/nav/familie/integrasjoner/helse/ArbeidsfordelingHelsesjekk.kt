package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.arbeidsfordeling.ArbeidsfordelingClient
import org.springframework.stereotype.Component

@Component
internal class ArbeidsfordelingHelsesjekk(arbeidsfordelingClient: ArbeidsfordelingClient)
    : AbstractHealthIndicator(arbeidsfordelingClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.arbeidsfordelingV1", "status", "nede")
}
