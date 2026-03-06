package no.nav.familie.integrasjoner.helse

import no.nav.familie.integrasjoner.client.rest.DokdistRestClient
import no.nav.familie.restklient.health.AbstractHealthIndicator
import org.springframework.stereotype.Component

@Component
internal class DokdistHelsesjekk(
    dokdistRestClient: DokdistRestClient,
) : AbstractHealthIndicator(dokdistRestClient, "helsesjekk.dokdist")
