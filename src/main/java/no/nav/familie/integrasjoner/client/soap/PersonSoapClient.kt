package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.springframework.stereotype.Component
import javax.xml.ws.soap.SOAPFaultException

@Component
class PersonSoapClient(private val port: PersonV3) : AbstractSoapClient("personV3"), Pingable {

    fun hentPersonResponse(request: HentPersonRequest?): HentPersonResponse {
        return try {
            port.hentPerson(request)
        } catch (e: SOAPFaultException) { // NOSONAR
            throw RuntimeException(e)
        }
    }

    /**
     * Henter personhistorikk i henhold til request
     *
     * @throws HentPersonhistorikkSikkerhetsbegrensning når bruker ikke har tilgang
     * @throws HentPersonhistorikkPersonIkkeFunnet      når bruker ikke finnes
     */
    fun hentPersonhistorikkResponse(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        return executeMedMetrics {
            port.hentPersonhistorikk(request)
        }
    }

    override fun ping() {
        port.ping()
    }

}
