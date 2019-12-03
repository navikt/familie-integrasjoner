package no.nav.familie.integrasjoner.helse

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.rest.OppgaveClient
import org.springframework.stereotype.Component

@Component
internal class OppgaveHelsesjekk(oppgaveClient: OppgaveClient) : AbstractHealthIndicator(oppgaveClient) {

    override val failureCounter: Counter = Metrics.counter("helsesjekk.oppgave", "status", "nede")
}