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
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component
import javax.xml.ws.soap.SOAPFaultException

@Component
class PersonSoapClient(private val port: PersonV3) : AbstractSoapClient("personV3"), Pingable {

    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Retryable(value = [OppslagException::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    fun hentPersonResponse(request: HentPersonRequest?): HentPersonResponse {
        return try {
            executeMedMetrics { port.hentPerson(request) }
        } catch (e: Exception) {
            when {
                e is HentPersonSikkerhetsbegrensning -> {
                    throw OppslagException("Ikke tilgang til å hente personinfo for person",
                                           "TPS.hentPerson",
                                           OppslagException.Level.LAV,
                                           HttpStatus.FORBIDDEN,
                                           e)
                }
                e is HentPersonPersonIkkeFunnet -> {
                    throw OppslagException("Prøver å hente person som ikke finnes i TPS",
                                           "TPS.hentPerson",
                                           OppslagException.Level.LAV,
                                           HttpStatus.NOT_FOUND,
                                           e)
                }
                sjekkOmExceptionErUnexpectedEOFinProlog(e) -> {
                    throw OppslagException("Unexpected EOF in prolog",
                                           "TPS.hentPerson",
                                           OppslagException.Level.LAV,
                                           HttpStatus.INTERNAL_SERVER_ERROR,
                                           e)
                }
                else -> {
                    throw OppslagException("Ukjent feil fra TPS",
                                           "TPS.hentPerson",
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
    @Retryable(value = [OppslagException::class], maxAttempts = 3, backoff = Backoff(delay = 4000))
    fun hentPersonhistorikkResponse(request: HentPersonhistorikkRequest?): HentPersonhistorikkResponse {
        return try {
            executeMedMetrics { port.hentPersonhistorikk(request) }
        } catch (e: Exception) {
            when {
                e is HentPersonSikkerhetsbegrensning -> {
                    throw OppslagException("Ikke tilgang til å hente personhistorikkinfo for person",
                                           "TPS.hentPersonhistorikk",
                                           OppslagException.Level.LAV,
                                           HttpStatus.FORBIDDEN,
                                           e)
                }
                e is HentPersonhistorikkPersonIkkeFunnet -> {
                    throw OppslagException("Prøver å hente historikk for person som ikke finnes i TPS",
                                           "TPS.hentPersonhistorikk",
                                           OppslagException.Level.LAV,
                                           HttpStatus.NOT_FOUND,
                                           e)
                }
                sjekkOmExceptionErUnexpectedEOFinProlog(e) -> {
                    throw OppslagException("Unexpected EOF in prolog",
                                           "TPS.hentPersonhistorikk",
                                           OppslagException.Level.LAV,
                                           HttpStatus.INTERNAL_SERVER_ERROR,
                                           e)
                }
                else -> {
                    throw OppslagException("Ukjent feil fra TPS",
                                           "TPS.hentPersonhistorikk",
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

    private fun sjekkOmExceptionErUnexpectedEOFinProlog(e: Exception): Boolean {
        return when {
            e is SOAPFaultException -> {
                secureLogger.info("message: ${e.message}, fault string: ${e.fault?.faultString}")
                e.message != null && e.message!!.contains("Unexpected EOF in prolog")
            }
            else -> false
        }
    }
}
