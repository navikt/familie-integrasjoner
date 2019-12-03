package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.integrasjoner.aktør.internal.AktørResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI
import java.util.concurrent.TimeUnit

@Component
class AktørregisterClient(@Value("\${AKTOERID_URL}")
                          private val aktørRegisterUrl: String,
                          @Qualifier("sts") restOperations: RestOperations,
                          private val stsRestClient: StsRestClient) : AbstractRestClient(restOperations) {

    override val pingUri: URI = URI.create(String.format("%s/client/isAlive", aktørRegisterUrl))

    private val aktoerResponstid =
            Metrics.timer("aktoer.respons.tid")
    private val aktoerSuccess =
            Metrics.counter("aktoer.response", "status", "success")
    private val aktoerFailure =
            Metrics.counter("aktoer.response", "status", "failure")

    fun hentAktørId(personIdent: String): AktørResponse {
        val uri = URI.create(String.format(PATH_HENT_AKTØR_ID,
                                           aktørRegisterUrl,
                                           AKTOERID_IDENTGRUPPE))
        return hentRespons(personIdent, uri)
    }

    fun hentPersonIdent(personIdent: String): AktørResponse {
        val uri = URI.create(String.format(PATH_HENT_PERSONIDENT,
                                           aktørRegisterUrl,
                                           PERSONIDENT_IDENTGRUPPE))
        return hentRespons(personIdent, uri)
    }

    private fun hentRespons(personIdent: String, uri: URI): AktørResponse {
        val httpHeaders = HttpHeaders().apply {
            add(NAV_PERSONIDENTER, personIdent)
        }

        try {
            val startTime = System.nanoTime()
            val aktørResponse = getForEntity<AktørResponse>(uri, httpHeaders)
            aktoerResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            aktoerSuccess.increment()
            return aktørResponse
        } catch (e: Exception) {
            aktoerFailure.increment()
            throw e
        }
    }

    companion object {
        private const val NAV_PERSONIDENTER = "Nav-Personidenter"
        private const val AKTOERID_IDENTGRUPPE = "AktoerId"
        private const val PERSONIDENT_IDENTGRUPPE = "NorskIdent"
        private const val PATH_HENT_AKTØR_ID = "%s/api/v1/identer?gjeldende=true&identgruppe=%s"
        private const val PATH_HENT_PERSONIDENT = "%s/api/v1/identer?gjeldende=true&identgruppe=%s"
    }

}