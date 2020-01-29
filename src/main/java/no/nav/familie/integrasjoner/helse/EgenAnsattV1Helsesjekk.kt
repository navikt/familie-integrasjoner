package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.soap.EgenAnsattSoapClient
import org.springframework.stereotype.Component

@Component
internal class EgenAnsattV1Helsesjekk(egenAnsattV1: EgenAnsattSoapClient)
    : AbstractHealthIndicator(egenAnsattV1, "helsesjekk.egenAnsattV1")
