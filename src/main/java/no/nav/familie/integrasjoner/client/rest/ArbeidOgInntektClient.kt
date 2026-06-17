package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.PersonIdent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class ArbeidOgInntektClient(
    @Value("\${ARBEID_INNTEKT_URL}") private val uri: URI,
    @Qualifier("utenAuthHttpClient") private val restClient: RestClient,
) {
    private val redirectUri =
        UriComponentsBuilder
            .fromUri(uri)
            .pathSegment("api/v2/redirect/sok/a-inntekt")
            .build()
            .toUri()

    fun hentUrlTilArbeidOgInntekt(personIdent: PersonIdent): String =
        try {
            restClient
                .get()
                .uri(redirectUri)
                .header(HttpHeaders.ACCEPT, MediaType.TEXT_PLAIN_VALUE)
                .header("Nav-Personident", personIdent.ident)
                .retrieve()
                .body<String>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved oppslag av url for a-inntekt.",
                "ainntekt.hentUrlTilArbeidOgInntekt",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
                "Kan ikke hente url for a-inntekt for $personIdent",
            )
        }
}
