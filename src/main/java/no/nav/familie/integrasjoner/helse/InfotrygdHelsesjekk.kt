package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.InfotrygdService
import org.springframework.stereotype.Component

@Component
internal class InfotrygdHelsesjekk(infotrygdService: InfotrygdService) : AbstractHealthIndicator(infotrygdService) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.infotrygd", "status", "nede")
}