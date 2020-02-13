package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.tjeneste.virksomhet.person.v3.binding.*
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import javax.xml.ws.soap.SOAPFaultException


@Component
class PersonSoapClient(private val port: PersonV3) : AbstractSoapClient("personV3"), Pingable {

    private val LOGGER = LoggerFactory.getLogger(PersonSoapClient::class.java)

    @Retryable(value = [OppslagException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
    fun hentPersonResponse(request: HentPersonRequest?): HentPersonResponse {
        return try {
            port.hentPerson(request)
        } catch (e: Exception) {
            when (e) {
                is HentPersonSikkerhetsbegrensning -> {
                    throw OppslagException("Ikke tilgang til å hente personinfo for person",
                                           "TPS",
                                           OppslagException.Level.LAV,
                                           HttpStatus.FORBIDDEN,
                                           e)
                }

                is HentPersonhistorikkPersonIkkeFunnet -> {
                    throw OppslagException("Prøver å hente historikk for person som ikke finnes i TPS",
                                           "TPS",
                                           OppslagException.Level.LAV,
                                           HttpStatus.NOT_FOUND,
                                           e)
                }

                else -> {
                    throw OppslagException("Ukjent feil fra TPS",
                                           "TPS",
                                           OppslagException.Level.KRITISK,
                                           HttpStatus.INTERNAL_SERVER_ERROR,
                                           e)
                }
            }
        }
    }

    /**
     * Henter personhistorikk i henhold til request
     *
     * @throws HentPersonhistorikkSikkerhetsbegrensning når bruker ikke har tilgang
     * @throws HentPersonhistorikkPersonIkkeFunnet      når bruker ikke finnes
     */
    @Retryable(value = [OppslagException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
    fun hentPersonhistorikkResponse(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        return try {
            executeMedMetrics { port.hentPersonhistorikk(request) }
        } catch (e: Exception) {
            when (e) {
                is HentPersonSikkerhetsbegrensning -> {
                    throw OppslagException("Ikke tilgang til å hente personinfo for person",
                                           "TPS",
                                           OppslagException.Level.LAV,
                                           HttpStatus.FORBIDDEN,
                                           e)
                }

                is HentPersonhistorikkPersonIkkeFunnet -> {
                    throw OppslagException("Prøver å hente historikk for person som ikke finnes i TPS",
                                           "TPS",
                                           OppslagException.Level.LAV,
                                           HttpStatus.NOT_FOUND,
                                           e)
                }

                else -> {
                    throw OppslagException("Ukjent feil fra TPS",
                                           "TPS",
                                           OppslagException.Level.KRITISK,
                                           HttpStatus.INTERNAL_SERVER_ERROR,
                                           e)
                }
            }
        }

    }

    override fun ping() {
        port.ping()
    }
}
