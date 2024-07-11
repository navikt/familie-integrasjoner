package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.personopplysning.PdlRequestException
import no.nav.familie.integrasjoner.personopplysning.internal.PdlBolkResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonBolkRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonBolkRequestVariables
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedRelasjonerOgAdressebeskyttelse
import no.nav.familie.kontrakter.felles.Tema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

/**
 * hentPersonBolk støtter ikke on-behalf-of token,
 * denne burde kun brukes for oppslag, og ikke for utlevering av data fra tjenesten
 */
@Service
class PdlClientCredentialRestClient(
    @Value("\${PDL_URL}") pdlBaseUrl: URI,
    @Qualifier("clientCredential") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "pdl.personinfo.cc") {
    private val pdlUri = UriUtil.uri(pdlBaseUrl, PATH_GRAPHQL)

    fun hentPersonMedRelasjonerOgAdressebeskyttelse(
        identer: List<String>,
        tema: Tema,
    ): Map<String, PdlPersonMedRelasjonerOgAdressebeskyttelse> {
        val request =
            PdlPersonBolkRequest(
                variables = PdlPersonBolkRequestVariables(identer),
                query = HENT_PERSON_RELASJONER_ADRESSEBESKYTTELSE,
            )
        val response =
            postForEntity<PdlBolkResponse<PdlPersonMedRelasjonerOgAdressebeskyttelse>>(
                pdlUri,
                request,
                pdlHttpHeaders(tema),
            )
        return feilsjekkOgReturnerData(response)
    }

    private inline fun <reified T : Any> feilsjekkOgReturnerData(pdlResponse: PdlBolkResponse<T>): Map<String, T> {
        if (pdlResponse.data == null) {
            secureLogger.error("Data fra pdl er null ved bolkoppslag av ${T::class} fra PDL: ${pdlResponse.errorMessages()}")
            throw PdlRequestException("Data er null fra PDL -  ${T::class}. Se secure logg for detaljer.")
        }

        val feil =
            pdlResponse.data.personBolk
                .filter { it.code != "ok" }
                .map { it.ident to it.code }
                .toMap()
        if (feil.isNotEmpty()) {
            secureLogger.error("Feil ved henting av ${T::class} fra PDL: $feil")
            throw PdlRequestException("Feil ved henting av ${T::class} fra PDL. Se secure logg for detaljer.")
        }
        if (pdlResponse.harAdvarsel()) {
            log.warn("Advarsel ved henting av ${T::class} fra PDL. Se securelogs for detaljer.")
            secureLogger.warn("Advarsel ved henting av ${T::class} fra PDL: ${pdlResponse.extensions?.warnings}")
        }
        return pdlResponse.data.personBolk.associateBy({ it.ident }, { it.person!! })
    }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
        private val HENT_PERSON_RELASJONER_ADRESSEBESKYTTELSE = graphqlQuery("/pdl/hentpersoner-relasjoner-adressebeskyttelse.graphql")
    }
}
