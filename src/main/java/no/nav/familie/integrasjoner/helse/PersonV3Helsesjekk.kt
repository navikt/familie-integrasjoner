package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import org.springframework.stereotype.Component

@Component
internal class PersonV3Helsesjekk(personSoapClient: PersonSoapClient)
    : AbstractHealthIndicator(personSoapClient, "helsesjekk.personV3")
