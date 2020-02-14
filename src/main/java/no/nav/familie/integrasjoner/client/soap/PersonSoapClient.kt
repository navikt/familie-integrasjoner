package no.nav.familie.integrasjoner.client.soap

import no.nav.familie.http.client.AbstractSoapClient
import no.nav.familie.http.client.Pingable
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkSikkerhetsbegrensning
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Component


@Component
class PersonSoapClient(private val port: PersonV3) : AbstractSoapClient("personV3"), Pingable {

    private val LOGGER = LoggerFactory.getLogger(PersonSoapClient::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")

    @Retryable(value = [OppslagException::class], maxAttempts = 3, backoff = Backoff(delay = 2000))
    fun hentPersonResponse(request: HentPersonRequest?): HentPersonResponse {
        return try {
            port.hentPerson(request)
        } catch (e: HentPersonSikkerhetsbegrensning) {
            throw OppslagException("Ikke tilgang til å hente personinfo for person",
                                   "TPS.hentPerson",
                                   OppslagException.Level.LAV,
                                   HttpStatus.FORBIDDEN,
                                   e)
        } catch (e: HentPersonhistorikkPersonIkkeFunnet) {
            throw OppslagException("Prøver å hente historikk for person som ikke finnes i TPS",
                                   "TPS.hentPerson",
                                   OppslagException.Level.LAV,
                                   HttpStatus.NOT_FOUND,
                                   e)
        } catch (e: Exception) {
            throw OppslagException("Ukjent feil fra TPS",
                                   "TPS.hentPerson",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
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
        } catch (e: HentPersonSikkerhetsbegrensning) {
            throw OppslagException("Ikke tilgang til å hente personinfo for person",
                                   "TPS.hentPersonhistorikk",
                                   OppslagException.Level.LAV,
                                   HttpStatus.FORBIDDEN,
                                   e)
        } catch (e: HentPersonhistorikkPersonIkkeFunnet) {
            throw OppslagException("Prøver å hente historikk for person som ikke finnes i TPS",
                                   "TPS.hentPersonhistorikk",
                                   OppslagException.Level.LAV,
                                   HttpStatus.NOT_FOUND,
                                   e)
        } catch (e: Exception) {
            throw OppslagException("Ukjent feil fra TPS",
                                   "TPS.hentPersonhistorikk",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    override fun ping() {
        port.ping()
    }
}
