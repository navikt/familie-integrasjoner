package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.azure.domene.Grupper
import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AzureGraphRestClient(@Qualifier("jwtBearer") restTemplate: RestOperations,
                           @Value("\${AAD_GRAPH_API_URI}") private val aadGraphURI: URI)
    : AbstractRestClient(restTemplate, "AzureGraph") {

    val saksbehandlerUri: URI = UriComponentsBuilder.fromUri(aadGraphURI).pathSegment(ME).build().toUri()

    val grupperUri: URI = UriComponentsBuilder.fromUri(aadGraphURI).pathSegment(ME, GRUPPER).build().toUri()


    fun hentSaksbehandler(): Saksbehandler {
        return getForEntity(saksbehandlerUri)
    }

    fun hentGrupper(): Grupper {
        return getForEntity(grupperUri)
    }

    companion object {
        private const val ME = "me"
        private const val GRUPPER = "memberOf"
    }
}
