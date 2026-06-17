package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.navkontor.NavKontorEnhet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class ArbeidsfordelingRestClient(
    @Value("\${NORG2_URL}")
    private val norg2Uri: URI,
    @Qualifier("utenAuthHttpClient")
    private val restClient: RestClient,
) {
    fun hentEnhet(geografiskOmråde: String): NavKontorEnhet =
        try {
            restClient
                .get()
                .uri(
                    UriComponentsBuilder
                        .fromUri(norg2Uri)
                        .pathSegment("api/v1/enhet/navkontor/$geografiskOmråde")
                        .build()
                        .toUri(),
                ).retrieve()
                .body<NavKontorEnhet>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av enhet",
                "norg2.hentEnhet",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    fun hentNavkontor(enhetId: String): NavKontorEnhet =
        try {
            restClient
                .get()
                .uri(
                    UriComponentsBuilder
                        .fromUri(norg2Uri)
                        .pathSegment("api/v1/enhet/$enhetId")
                        .build()
                        .toUri(),
                ).retrieve()
                .body<NavKontorEnhet>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av navkontor",
                "norg2.hentNavkontor",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    fun finnBehandlendeEnhetMedBesteMatch(arbeidsfordelingskriterie: ArbeidsfordelingKriterie): List<Enhet> =
        try {
            restClient
                .post()
                .uri(
                    UriComponentsBuilder
                        .fromUri(norg2Uri)
                        .pathSegment("api/v1/arbeidsfordeling/enheter/bestmatch")
                        .build()
                        .toUri(),
                ).body(arbeidsfordelingskriterie)
                .retrieve()
                .body<List<NavKontorEnhet>>()!!
                .map { Enhet(enhetId = it.enhetNr, enhetNavn = it.navn) }
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved oppslag av best matchende behandlende enhet",
                "norg2.finnBehandlendeEnhetMedBesteMatch",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
}
