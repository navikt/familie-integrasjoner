package no.nav.familie.integrasjoner.client.soap

import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.client.Pingable
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.springframework.stereotype.Component
import java.util.concurrent.TimeUnit
import javax.xml.ws.soap.SOAPFaultException

@Component
class PersonSoapClient(private val port: PersonV3) : Pingable {
    private val personResponsTid =
            Metrics.timer("personV3.respons.tid")
    private val personSuccess =
            Metrics.counter("personV3.response", "status", "success")
    private val personFailure =
            Metrics.counter("personV3.response", "status", "failure")

    @Throws(HentPersonPersonIkkeFunnet::class, HentPersonSikkerhetsbegrensning::class)
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
     * @param request request
     * @return respons
     * @throws HentPersonhistorikkSikkerhetsbegrensning når bruker ikke har tilgang
     * @throws HentPersonhistorikkPersonIkkeFunnet      når bruker ikke finnes
     */
    @Throws(HentPersonhistorikkSikkerhetsbegrensning::class, HentPersonhistorikkPersonIkkeFunnet::class)
    fun hentPersonhistorikkResponse(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        return try {
            val startTime = System.nanoTime()
            val response =
                    port.hentPersonhistorikk(request)
            personResponsTid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            personSuccess.increment()
            response
        } catch (e: SOAPFaultException) { // NOSONAR
            personFailure.increment()
            throw RuntimeException(e)
        }
    }

    override fun ping() {
        port.ping()
    }

}