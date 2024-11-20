package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.integrasjoner.personopplysning.PdlUnauthorizedException
import no.nav.familie.integrasjoner.personopplysning.internal.PdlResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PipPersonDataResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PipPersondataResponseList
import no.nav.familie.kontrakter.felles.Tema
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class PdlPipRestClient(
    @Value("\${PDL_PIP_URL}") pdlPipBaseUrl: URI,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
) : AbstractRestClient(restTemplate, "pdl.personinfo") {
    private val pdlUri = UriUtil.uri(pdlPipBaseUrl, PATH_GRAPHQL)

    fun hentPersonMedAdressebeskyttelse(
        personIdent: String,
        tema: Tema,
    ): PipPersonDataResponse {


        val uriMedParams = UriComponentsBuilder.fromUri(pdlUri)
            .queryParam("ident", personIdent)
            .build()
            .toUri()

        val response: PipPersonDataResponse =
            getForEntity(
                uriMedParams,
                pdlHttpHeaders(tema),
            )

        return response
    }

    fun hentPersonerMedAdressebeskyttelse(
        personIdenter: List<String>,
        tema: Tema,
    ): PipPersondataResponseList {


        val uri = UriComponentsBuilder.fromUri(pdlUri)
            .build()
            .toUri()

        val response: PipPersondataResponseList =
            postForEntity(
                uri = uri,
                httpHeaders = pdlHttpHeaders(tema),
                payload = personIdenter,
            )

        return response
    }




    private inline fun <reified DATA : Any, reified RESPONSE : Any> feilsjekkOgReturnerData(
        pdlResponse: PdlResponse<DATA>,
        personIdent: String,
        dataMapper: (DATA) -> RESPONSE?,
    ): RESPONSE {
        if (pdlResponse.harFeil()) {
            if (pdlResponse.harNotFoundFeil()) {
                secureLogger.info("Finner ikke person for ident=$personIdent i PDL")
                throw PdlNotFoundException()
            }
            if (pdlResponse.harUnauthorizedFeil()) {
                secureLogger.info("Har ikke tilgang til person med ident=$personIdent i PDL")
                throw PdlUnauthorizedException()
            }
            throw pdlOppslagException(
                feilmelding = "Feil ved oppslag på person: ${pdlResponse.errorMessages()}. Se secureLogs for mer info.",
                personIdent = personIdent,
            )
        }
        if (pdlResponse.harAdvarsel()) {
            log.warn("Advarsel ved henting av ${DATA::class} fra PDL. Se securelogs for detaljer.")
            secureLogger.warn("Advarsel ved henting av ${DATA::class} fra PDL: ${pdlResponse.extensions?.warnings}")
        }
        val data =
            dataMapper.invoke(pdlResponse.data)
                ?: throw pdlOppslagException(
                    feilmelding = "Feil ved oppslag på person. Objekt mangler på responsen fra PDL. Se secureLogs for mer info.",
                    personIdent = personIdent,
                )
        return data
    }



    private fun pdlOppslagException(
        personIdent: String,
        httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        error: Throwable? = null,
        feilmelding: String = "Feil ved oppslag på person. Gav feil: ${error?.message}",
    ): OppslagException {
        responsFailure.increment()
        return OppslagException(
            feilmelding,
            "PdlRestClient",
            OppslagException.Level.MEDIUM,
            httpStatus,
            error,
            personIdent,
        )
    }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
        private val HENT_PERSON = graphqlQuery("/pdl/hentperson-enkel.graphql")
        private val HENT_IDENTER_QUERY = graphqlQuery("/pdl/hentIdenter.graphql")
        private val HENT_GEOGRAFISK_TILKNYTNING_QUERY = graphqlQuery("/pdl/geografisk_tilknytning.graphql")
        private val HENT_ADRESSEBESKYTTELSE_QUERY = graphqlQuery("/pdl/adressebeskyttelse.graphql")

        private fun pdlHttpHeaders(tema: Tema): HttpHeaders =
            HttpHeaders().apply {
                contentType = MediaType.APPLICATION_JSON
                accept = listOf(MediaType.APPLICATION_JSON)
                add("Tema", tema.name)
                add("behandlingsnummer", tema.behandlingsnummer)
            }

    }
}


