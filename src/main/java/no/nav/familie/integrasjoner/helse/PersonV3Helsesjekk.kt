package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.soap.PersonConsumer
import org.springframework.stereotype.Component

@Component
internal class PersonV3Helsesjekk(personConsumer: PersonConsumer) : AbstractHealthIndicator(personConsumer) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.personV3", "status", "nede")
}