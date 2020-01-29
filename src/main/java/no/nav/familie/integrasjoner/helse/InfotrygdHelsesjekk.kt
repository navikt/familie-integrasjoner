package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.InfotrygdRestClient
import org.springframework.stereotype.Component

@Component
internal class InfotrygdHelsesjekk(infotrygdRestClient: InfotrygdRestClient)
    : AbstractHealthIndicator(infotrygdRestClient, "helsesjekk.infotrygd")
