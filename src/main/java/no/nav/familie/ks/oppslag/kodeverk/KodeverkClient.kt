package no.nav.familie.ef.mottak.api.kodeverk

import no.nav.familie.ef.mottak.api.kodeverk.domene.BetydningerDto
import no.nav.familie.ef.mottak.api.kodeverk.domene.PostnummerDto
import no.nav.familie.ef.mottak.api.kodeverk.domene.PoststedDto
import no.nav.familie.ks.oppslag.config.KodeverkConfig
import org.springframework.stereotype.Component

@Component
class KodeverkClient(val config: KodeverkConfig) { //val httpClient
    fun hentPostnummerBetydninger(): PostnummerDto {
        val språkMock = PoststedDto("Oslo", "Oslo")
        val betydningerMock = BetydningerDto("00.00.0000", "99.99.9999", mapOf("nb" to språkMock))
        val mockResponse = PostnummerDto(mapOf("0556" to listOf(betydningerMock)))
        return mockResponse/*
        val request = HttpRequest.newBuilder()
                .uri(config.postnummerUri)
                .header(javax.ws.rs.core.HttpHeaders.ACCEPT, "application/json")
                .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), config.consumer)
                .build()
        return try {
            val httpResponse: HttpResponse<String> = httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            objectMapper.readValue(httpResponse.body(), PostnummerDto::class.java)
        } catch (e: IOException ) {
            throw RuntimeException("Feil ved kall mot kodeverk ", e)
        } catch (e: InterruptedException) {
            throw RuntimeException("Feil ved kall mot kodeverk", e)
        }
        */
    }

}

