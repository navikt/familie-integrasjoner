package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.azure.domene.Saksbehandler
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class AzureGraphRestClient(@Qualifier("azure") restTemplate: RestOperations,
                           @Value("\${AAD_GRAPH_API_URI}") private val aadGrapURI: URI)
    : AbstractRestClient(restTemplate, "AzureGraph") {

    val saksbehandler: Saksbehandler
        get() {

            return getForEntity(UriUtil.uri(aadGrapURI, PATH))
        }

    companion object {
        private const val PATH = "me?\$select=displayName,onPremisesSamAccountName,userPrincipalName"
    }

}