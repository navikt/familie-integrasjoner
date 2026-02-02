package no.nav.familie.integrasjoner.helse

import no.nav.familie.integrasjoner.client.rest.AaregRestClient
import no.nav.familie.restklient.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
internal class AaregHelsesjekk(
    aaregRestClient: AaregRestClient,
) : AbstractHealthIndicator(aaregRestClient, "helsesjekk.aareg")
