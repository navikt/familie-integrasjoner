package no.nav.familie.integrasjoner.arbeidsfordeling

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.arbeidsfordeling.Enhet
import no.nav.familie.kontrakter.felles.navkontor.NavKontorEnhet
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class ArbeidsfordelingRestClient(
    @Value("\${NORG2_URL}")
    private val norg2Uri: URI,
    @Qualifier("noAuthorize")
    restOperations: RestOperations,
) : AbstractPingableRestClient(restOperations, "norg2") {
    fun hentEnhet(geografiskOmråde: String): NavKontorEnhet =
        try {
            getForEntity(
                UriComponentsBuilder
                    .fromUri(norg2Uri)
                    .pathSegment("api/v1/enhet/navkontor/$geografiskOmråde")
                    .build()
                    .toUri(),
            )
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
            getForEntity(
                UriComponentsBuilder
                    .fromUri(norg2Uri)
                    .pathSegment("api/v1/enhet/$enhetId")
                    .build()
                    .toUri(),
            )
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
            postForEntity<List<NavKontorEnhet>>(
                UriComponentsBuilder
                    .fromUri(norg2Uri)
                    .pathSegment("api/v1/arbeidsfordeling/enheter/bestmatch")
                    .build()
                    .toUri(),
                arbeidsfordelingskriterie,
            ).map { Enhet(enhetId = it.enhetNr, enhetNavn = it.navn) }
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved oppslag av best matchende behandlende enhet",
                "norg2.finnBehandlendeEnhetMedBesteMatch",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    override val pingUri: URI
        get() =
            UriComponentsBuilder
                .fromUri(norg2Uri)
                .pathSegment("api/ping")
                .build()
                .toUri()
}
