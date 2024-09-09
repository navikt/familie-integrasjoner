package no.nav.familie.integrasjoner.enhet

import no.nav.familie.integrasjoner.client.rest.AxsysRestClient
import org.springframework.stereotype.Service

@Service
class AxsysService(
    private val axsysRestClient: AxsysRestClient
) {

    fun hentTilgang(saksbehanderId: SaksbehandlerId): TilgangV2DTO {
        return axsysRestClient.hentTilgang(saksbehanderId)
    }

}