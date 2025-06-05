package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class ArbeidOgInntektClient(
    @Value("\${ARBEID_INNTEKT_URL}") private val uri: URI,
    @Qualifier("jwtBearer") private val restOperations: RestOperations,
) : AbstractRestClient(restOperations, "ainntekt") {
    private val redirectUri =
        UriComponentsBuilder
            .fromUri(uri)
            .pathSegment("api/v2/redirect/sok/a-inntekt")
            .build()
            .toUri()

    fun hentUrlTilArbeidOgInntekt(personIdent: String): String =
        getForEntity(
            redirectUri,
            HttpHeaders().apply {
                accept = listOf(MediaType.TEXT_PLAIN)
                set("Nav-Personident", personIdent)
            },
        )
}
