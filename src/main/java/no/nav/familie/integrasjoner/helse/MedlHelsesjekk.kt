package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.rest.MedlRestClient
import org.springframework.stereotype.Component

@Component
internal class MedlHelsesjekk(medlRestClient: MedlRestClient)
    : AbstractHealthIndicator(medlRestClient, "helsesjekk.medl")
