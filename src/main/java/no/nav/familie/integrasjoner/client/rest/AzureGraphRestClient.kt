package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.azure.domene.AzureAdBruker
import no.nav.familie.integrasjoner.azure.domene.AzureAdBrukere
import no.nav.familie.integrasjoner.azure.domene.Grupper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AzureGraphRestClient(
    @Qualifier("jwtBearer") restTemplate: RestOperations,
    @Value("\${AAD_GRAPH_API_URI}") private val aadGraphURI: URI,
) : AbstractRestClient(restTemplate, "AzureGraph") {
    val saksbehandlerUri: URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(ME)
            .build()
            .toUri()

    fun saksbehandlerUri(id: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS, id)
            .queryParam("\$select", FELTER)
            .build()
            .toUri()

    fun saksbehandlersøkUri(navIdent: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS)
            .queryParam("\$search", "\"onPremisesSamAccountName:{navIdent}\"")
            .queryParam("\$select", FELTER)
            .buildAndExpand(navIdent)
            .toUri()

    val grupperUri: URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(ME, GRUPPER)
            .build()
            .toUri()

    fun finnSaksbehandler(navIdent: String): AzureAdBrukere =
        getForEntity(
            saksbehandlersøkUri(navIdent),
            HttpHeaders().apply {
                add("ConsistencyLevel", "eventual")
            },
        )

    fun hentSaksbehandler(): AzureAdBruker = getForEntity(saksbehandlerUri)

    fun hentSaksbehandler(id: String): AzureAdBruker = getForEntity(saksbehandlerUri(id))

    fun hentGrupper(): Grupper = getForEntity(grupperUri)

    companion object {
        private const val ME = "me"
        private const val USERS = "users"
        private const val GRUPPER = "memberOf"
        private const val FELTER = "givenName,surname,onPremisesSamAccountName,id,userPrincipalName,streetAddress"
    }
}
