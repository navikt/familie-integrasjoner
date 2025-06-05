package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.medlemskap.MedlemskapsunntakResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class MedlRestClient(
    @Value("\${MEDL2_URL}") private val medl2BaseUrl: URI,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
) : AbstractPingableRestClient(restTemplate, "medlemskap") {
    override val pingUri: URI =
        UriComponentsBuilder
            .fromUri(medl2BaseUrl)
            .pathSegment(PATH_PING)
            .build()
            .toUri()

    val medlemskapsunntakUri: URI =
        UriComponentsBuilder
            .fromUri(medl2BaseUrl)
            .pathSegment(PATH_MEDLEMSKAPSUNNTAK)
            .build()
            .toUri()

    fun hentMedlemskapsUnntakResponse(
        personident: String,
        type: String? = null,
        statuser: List<String>? = null,
        ekskluderKilder: List<String>? = null,
        fraOgMed: String? = null,
        tilOgMed: String? = null,
        inkluderSporingsinfo: Boolean? = null
    ): List<MedlemskapsunntakResponse> {
        val requestBody = PeriodeSoekRequest(
            personident = personident,
            type = type,
            statuser = statuser,
            ekskluderKilder = ekskluderKilder,
            fraOgMed = fraOgMed,
            tilOgMed = tilOgMed,
            inkluderSporingsinfo = inkluderSporingsinfo
        )

        try {
            return restTemplate.postForObject(
                medlemskapsunntakUri,
                requestBody,
                Array<MedlemskapsunntakResponse>::class.java
            )?.toList() ?: emptyList()
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av medlemskapsunntak",
                "medl.unntak",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    companion object {
        private const val PATH_PING = "api/ping"
        private const val PATH_MEDLEMSKAPSUNNTAK = "rest/v1/periode/soek"
    }
}

data class PeriodeSoekRequest(
    val personident: String,
    val type: String? = null,
    val statuser: List<String>? = null,
    val ekskluderKilder: List<String>? = null,
    val fraOgMed: String? = null,
    val tilOgMed: String? = null,
    val inkluderSporingsinfo: Boolean? = null,
)
