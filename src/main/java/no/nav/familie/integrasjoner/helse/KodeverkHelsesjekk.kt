package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.KodeverkClient
import org.springframework.stereotype.Component

@Component
internal class KodeverkHelsesjekk(kodeverkClient: KodeverkClient) :
    AbstractHealthIndicator(kodeverkClient, "helsesjekk.kodeverk")
