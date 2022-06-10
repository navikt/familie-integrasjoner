package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import org.springframework.stereotype.Component

@Component
internal class SafHelsesjekk(safRestClient: SafRestClient) :
    AbstractHealthIndicator(safRestClient, "helsesjekk.saf")
