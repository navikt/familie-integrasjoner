package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import org.springframework.stereotype.Component

@Component
internal class OppgaveHelsesjekk(oppgaveRestClient: OppgaveRestClient) : AbstractHealthIndicator(oppgaveRestClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.oppgave", "status", "nede")
}