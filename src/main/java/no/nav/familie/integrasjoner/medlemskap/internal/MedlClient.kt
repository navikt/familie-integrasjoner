package no.nav.familie.integrasjoner.medlemskap.internal

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.http.client.HttpRequestUtil
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.integrasjoner.medlemskap.MedlemskapsUnntakResponse
import no.nav.familie.log.NavHttpHeaders
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.util.*
import javax.ws.rs.core.HttpHeaders

class MedlClient(private val medl2BaseUrl: String,
                 srvBruker: String,
                 stsRestClient: StsRestClient,
                 objectMapper: ObjectMapper) {
    val medl2Uri: URI
    private val httpClient: HttpClient
    private val stsRestClient: StsRestClient
    private val srvBruker: String
    private val objectMapper: ObjectMapper
    fun hentMedlemskapsUnntakResponse(aktørId: String?): List<MedlemskapsUnntakResponse> {
        val request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.systemOIDCToken)
                .uri(medl2Uri)
                .header(HttpHeaders.ACCEPT, "application/json")
                .header(NavHttpHeaders.NAV_PERSONIDENT.asString(), aktørId)
                .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), srvBruker)
                .build()
        return try {
            val httpResponse =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            if (HttpStatus.OK.value() != httpResponse.statusCode() || httpResponse.body().isEmpty()) {
                LOG.warn("Medl2 returnerte feil. Responskode: {}. Respons: {}",
                         httpResponse.statusCode(),
                         httpResponse.body())
                throw RuntimeException("Feil ved kall til MEDL2")
            }
            Arrays.asList(*objectMapper.readValue(httpResponse.body(),
                                                  Array<MedlemskapsUnntakResponse>::class.java))
        } catch (e: IOException) {
            throw RuntimeException("Feil ved kall til MEDL2", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("Feil ved kall til MEDL2", e)
        }
    }

    @Throws(Exception::class) fun ping() {
        val pingURI = URI.create(String.format("%s/internal/isAlive", medl2BaseUrl))
        val request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.systemOIDCToken)
                .uri(pingURI)
                .build()
        val response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (HttpStatus.OK.value() != response.statusCode()) {
            throw Exception("Feil ved ping til MEDL")
        }
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(MedlClient::class.java)
    }

    init {
        medl2Uri = URI.create(String.format("%s/api/v1/medlemskapsunntak", medl2BaseUrl))
        this.srvBruker = srvBruker
        this.stsRestClient = stsRestClient
        httpClient = HttpClient.newHttpClient()
        this.objectMapper = objectMapper
    }
}