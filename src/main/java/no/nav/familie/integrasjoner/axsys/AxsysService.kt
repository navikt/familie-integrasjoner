package no.nav.familie.integrasjoner.axsys

import no.nav.familie.integrasjoner.client.rest.AxsysRestClient
import org.springframework.stereotype.Service

@Service
class AxsysService(
    private val axsysRestClient: AxsysRestClient,
) {
    fun hentTilgang(saksbehanderId: SaksbehandlerId): TilgangV2DTO = axsysRestClient.hentTilgang(saksbehanderId)
}
