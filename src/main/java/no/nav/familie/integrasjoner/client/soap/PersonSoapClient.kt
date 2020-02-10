package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.slf4j.LoggerFactory
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import javax.xml.ws.soap.SOAPFaultException


@Component
class PersonSoapClient(private val port: PersonV3) : AbstractSoapClient("personV3"), Pingable {

    private val LOGGER = LoggerFactory.getLogger(PersonSoapClient::class.java)

    @Retryable(value = [SOAPFaultException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
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
     * @throws HentPersonhistorikkSikkerhetsbegrensning når bruker ikke har tilgang
     * @throws HentPersonhistorikkPersonIkkeFunnet      når bruker ikke finnes
     */
    @Retryable(value = [SOAPFaultException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
    @Throws(HentPersonhistorikkSikkerhetsbegrensning::class, HentPersonhistorikkPersonIkkeFunnet::class)
    fun hentPersonhistorikkResponse(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        return try {
            executeMedMetrics { port.hentPersonhistorikk(request) }
        } catch (e: SOAPFaultException) { // NOSONAR
            throw RuntimeException(e)
        }

    }

    override fun ping() {
        port.ping()
    }

    @Recover
    fun hentPersonFallback(e: SOAPFaultException): HentPersonhistorikkResponse {
        LOGGER.error("kall mot TPS feilet 3 ganger, gir opp...")
        throw e
    }
}
