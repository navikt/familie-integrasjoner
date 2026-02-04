package no.nav.familie.integrasjoner.helse

import no.nav.familie.integrasjoner.client.rest.DokarkivRestClient
import no.nav.familie.restklient.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
internal class DokarkivHelsesjekk(
    dokarkivRestClient: DokarkivRestClient,
) : AbstractHealthIndicator(dokarkivRestClient, "helsesjekk.dokarkiv")
