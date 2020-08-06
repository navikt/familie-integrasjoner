package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.AaregRestClient
import org.springframework.stereotype.Component

@Component
internal class AaregHelsesjekk(aaregRestClient: AaregRestClient)
    : AbstractHealthIndicator(aaregRestClient, "helsesjekk.aareg")
