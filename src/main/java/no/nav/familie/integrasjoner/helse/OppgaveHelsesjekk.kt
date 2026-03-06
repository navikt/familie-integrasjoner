package no.nav.familie.integrasjoner.helse

import no.nav.familie.integrasjoner.client.rest.OppgaveRestClient
import no.nav.familie.restklient.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
internal class OppgaveHelsesjekk(
    oppgaveRestClient: OppgaveRestClient,
) : AbstractHealthIndicator(oppgaveRestClient, "helsesjekk.oppgave")
