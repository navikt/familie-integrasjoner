package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.arbeidsfordeling.ArbeidsfordelingClient
import org.springframework.stereotype.Component

@Component
internal class ArbeidsfordelingHelsesjekk(arbeidsfordelingClient: ArbeidsfordelingClient) :
    AbstractHealthIndicator(arbeidsfordelingClient, "helsesjekk.arbeidsfordelingV1")
