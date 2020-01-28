package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import org.springframework.stereotype.Component

@Component
internal class DokarkivHelsesjekk(dokarkivRestClient: DokarkivRestClient)
    : AbstractHealthIndicator(dokarkivRestClient, "helsesjekk.dokarkiv")
