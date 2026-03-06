package no.nav.familie.integrasjoner.helse

import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import no.nav.familie.restklient.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
internal class EgenAnsattHelsesjekk(
    egenAnsattRestClient: EgenAnsattRestClient,
) : AbstractHealthIndicator(egenAnsattRestClient, "helsesjekk.egenansatt")
