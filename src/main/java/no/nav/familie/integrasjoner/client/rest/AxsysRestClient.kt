package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.axsys.TilgangV2DTO
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.NavIdent
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AxsysRestClient(
    @Value("\${AXSYS_URL}") private val enhetBaseUrl: URI,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "enhetstilganger") {
    fun hentEnheterNavIdentHarTilgangTil(navIdent: NavIdent): TilgangV2DTO {
        val uri =
            UriComponentsBuilder
                .fromUri(enhetBaseUrl)
                .pathSegment("v2/tilgang/${navIdent.ident}")
                .build()
                .toUri()

        return try {
            getForEntity<TilgangV2DTO>(uri)
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av enheter Nav identer har tilgang til",
                "axsys.hentEnheterNavIdentHarTilgangTil",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }
}
