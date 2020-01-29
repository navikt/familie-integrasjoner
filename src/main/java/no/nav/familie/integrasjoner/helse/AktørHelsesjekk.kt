package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.AktørregisterRestClient
import org.springframework.stereotype.Component

@Component
internal class AktørHelsesjekk(aktørregisterRestClient: AktørregisterRestClient)
    : AbstractHealthIndicator(aktørregisterRestClient, "helsesjekk.aktoer")
