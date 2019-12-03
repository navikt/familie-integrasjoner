package no.nav.familie.integrasjoner.client.rest

import com.fasterxml.jackson.databind.ObjectMapper
import no.nav.familie.integrasjoner.config.KodeverkConfig
import no.nav.familie.integrasjoner.kodeverk.domene.PostnummerDto
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class KodeverkClient(val config: KodeverkConfig,
                     val objectMapper: ObjectMapper,
                     @Qualifier("sts") val restTemplate: RestOperations) {

    fun hentPostnummerBetydninger(): PostnummerDto {
        val response = restTemplate.getForEntity(config.postnummerUri,
                                                 PostnummerDto::class.java)
        return response.body
    }

    @Throws(Exception::class) fun ping() {
        val pingURI = URI.create(String.format("%s/client/isAlive", config.KODEVERK_URL))
        val response: ResponseEntity<String> = restTemplate.exchange(pingURI,
                                                                     HttpMethod.GET,
                                                                     null,
                                                                     String::class.java)
        if (HttpStatus.OK.value() != response.statusCodeValue) {
            throw Exception("Feil ved ping til kodeverk")
        }
    }
}
