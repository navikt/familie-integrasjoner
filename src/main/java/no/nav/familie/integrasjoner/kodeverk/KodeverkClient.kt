package no.nav.familie.integrasjoner.kodeverk

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.http.client.NavHttpHeaders.NAV_CALLID
import no.nav.familie.http.client.NavHttpHeaders.NAV_CONSUMER_ID
import no.nav.familie.integrasjoner.config.KodeverkConfig
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.kodeverk.domene.PostnummerDto
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate
import java.io.IOException
import java.net.URI

@Component
class KodeverkClient(val config: KodeverkConfig,
                     val objectMapper: ObjectMapper,
                     val restTemplate: RestTemplate) {

    fun hentPostnummerBetydninger(): PostnummerDto {
        val headers = HttpHeaders().apply {
            add(NAV_CALLID.asString(), MDCOperations.getCallId())
            add(NAV_CONSUMER_ID.asString(), config.consumer)
        }
        val response = restTemplate.exchange(config.KODEVERK_URL,
                                             HttpMethod.GET,
                                             HttpEntity(null, headers),
                                             PostnummerDto::class.java)
        return response.body
    }

    @Throws(Exception::class) fun ping() {
        val pingURI = URI.create(String.format("%s/internal/isAlive", config.KODEVERK_URL))
        val response : ResponseEntity<String> = restTemplate.exchange(pingURI,
                                                                      HttpMethod.GET,
                                                                      null,
                                                                      String::class.java)
        if (HttpStatus.OK.value() != response.statusCodeValue) {
            throw Exception("Feil ved ping til kodeverk")
        }
    }
}
