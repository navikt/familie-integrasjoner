package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.soap.EgenAnsattConsumer
import org.springframework.stereotype.Component

@Component
internal class EgenAnsattV1Helsesjekk(egenAnsattV1: EgenAnsattConsumer) : AbstractHealthIndicator(egenAnsattV1) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.egenAnsattV1", "status", "nede")
}