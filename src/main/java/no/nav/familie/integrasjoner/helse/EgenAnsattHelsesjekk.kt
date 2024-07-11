package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.EgenAnsattRestClient
import org.springframework.stereotype.Component

@Component
internal class EgenAnsattHelsesjekk(
    egenAnsattRestClient: EgenAnsattRestClient,
) : AbstractHealthIndicator(egenAnsattRestClient, "helsesjekk.egenansatt")
