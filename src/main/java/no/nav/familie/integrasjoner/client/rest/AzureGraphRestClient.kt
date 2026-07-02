package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.azure.domene.AzureAdBruker
import no.nav.familie.integrasjoner.azure.domene.AzureAdBrukere
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.kontrakter.felles.saksbehandler.SaksbehandlerGrupper
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class AzureGraphRestClient(
    @Value("\${AAD_GRAPH_API_URI}") private val aadGraphURI: URI,
    @Value("\${AAD_GRAPH_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt().tokenValue }

    private fun saksbehandlerUri(id: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS, id)
            .queryParam("\$select", FELTER)
            .build()
            .toUri()

    private fun saksbehandlersøkUri(navIdent: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS)
            .queryParam("\$search", "\"onPremisesSamAccountName:{navIdent}\"")
            .queryParam("\$select", FELTER)
            .buildAndExpand(navIdent)
            .toUri()

    private fun hentGruppeneTilSaksbehandlerUri(azureUUID: String): URI =
        UriComponentsBuilder
            .fromUri(aadGraphURI)
            .pathSegment(USERS)
            .pathSegment(azureUUID)
            .pathSegment(GRUPPER)
            .queryParam("\$top", MAX_ANTALL_GRUPPER)
            .build()
            .toUri()

    fun finnSaksbehandler(navIdent: String): AzureAdBrukere =
        try {
            restClient
                .get()
                .uri(saksbehandlersøkUri(navIdent))
                .header("ConsistencyLevel", "eventual")
                .retrieve()
                .body<AzureAdBrukere>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av saksbehandler med nav ident",
                "azure.saksbehandler.navIdent",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    fun hentGruppeneTilSaksbehandler(azureId: String): SaksbehandlerGrupper =
        try {
            restClient
                .get()
                .uri(hentGruppeneTilSaksbehandlerUri(azureId))
                .header("ConsistencyLevel", "eventual")
                .retrieve()
                .body<SaksbehandlerGrupper>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av saksbehandlers grupper med azure id",
                "azure.saksbehandler.id",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    fun hentSaksbehandler(id: String): AzureAdBruker =
        try {
            restClient
                .get()
                .uri(saksbehandlerUri(id))
                .retrieve()
                .body<AzureAdBruker>()!!
        } catch (e: Exception) {
            throw OppslagException(
                "Feil ved henting av saksbehandler med id",
                "azure.saksbehandler.id",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }

    companion object {
        private const val USERS = "users"
        private const val GRUPPER = "memberOf"
        private const val FELTER = "givenName,surname,onPremisesSamAccountName,id,userPrincipalName,streetAddress,city"
        private const val MAX_ANTALL_GRUPPER = 250
    }
}
