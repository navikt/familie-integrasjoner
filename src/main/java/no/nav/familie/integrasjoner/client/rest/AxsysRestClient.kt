package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.axsys.SaksbehandlerId
import no.nav.familie.integrasjoner.axsys.TilgangV2DTO
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AxsysRestClient(
    @Value("\${AXSYS_URL}") private val enhetBaseUrl: URI,
    @Qualifier("sts") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "enhet") {
    fun hentTilgang(saksbehandlerId: SaksbehandlerId): TilgangV2DTO {
        val uri =
            UriComponentsBuilder
                .fromUri(enhetBaseUrl)
                .pathSegment("v2/tilgang/${saksbehandlerId.verdi}")
                .build()
                .toUri()
        return getForEntity<TilgangV2DTO>(uri)
    }
}
