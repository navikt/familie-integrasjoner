package no.nav.familie.ef.mottak.api.kodeverk

import no.nav.familie.ef.mottak.api.kodeverk.domene.PostnummerDto
import no.nav.familie.http.client.NavHttpHeaders
import no.nav.familie.ks.kontrakter.objectMapper
import no.nav.familie.ks.oppslag.config.KodeverkConfig
import org.springframework.stereotype.Component
import java.io.IOException
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse

@Component
class KodeverkClient(val config: KodeverkConfig) { //val httpClient
    val httpClient : HttpClient = HttpClient.newHttpClient()
    fun hentPostnummerBetydninger(): PostnummerDto {
        val request = HttpRequest.newBuilder()
                .uri(config.postnummerUri)
                .header(javax.ws.rs.core.HttpHeaders.ACCEPT, "application/json")
                .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), config.consumer)
                .build()
        return try {
            val httpResponse: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            objectMapper.readValue(httpResponse.body(), PostnummerDto::class.java)
        } catch (e: IOException) {
            throw RuntimeException("Feil ved kall mot kodeverk ", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("Feil ved kall mot kodeverk", e)
        }
    }

    /*
    @Throws(Exception::class) fun ping() {
        val pingURI = URI.create(String.format("%s/internal/isAlive", config.KODEVERK_URL))
        val request = HttpRequest.newBuilder()
                .uri(pingURI)
                .build()
        val response: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (HttpStatus.OK.value() != response.statusCode()) {
            throw Exception("Feil ved ping til kodeverk")
        }
    }

     */

}

