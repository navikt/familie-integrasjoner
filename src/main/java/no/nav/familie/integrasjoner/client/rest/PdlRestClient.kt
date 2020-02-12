package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsDato
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsdatoRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlFødselsdatoResponse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlRequestVariable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import java.net.URI

@Service
class PdlRestClient(@Value("\${PDL_URL}") pdlBaseUrl: URI,
                    @Qualifier("sts") val restTemplate: RestOperations,
                    private val stsRestClient: StsRestClient)
    : AbstractRestClient(restTemplate, "pdl.personinfo") {

    private val pdlUri = UriUtil.uri(pdlBaseUrl, PATH_GRAPHQL)

    fun hentFødselsdato(personIdent: String, tema: String): PdlFødselsDato {
        val pdlFødselsdatoRequest = PdlFødselsdatoRequest(PdlRequestVariable(personIdent))
        try {
            val response = postForEntity<PdlFødselsdatoResponse>(pdlUri,
                                                                 pdlFødselsdatoRequest,
                                                                 httpHeaders())

            if (response != null && !response.harFeil()) {
                return  response?.data?.person?.foedsel?.get(0) ?:
                        throw OppslagException("Fant ikke forespurte data på person $personIdent",
                                               "PdlRestClient",
                                               OppslagException.Level.LAV,
                                               HttpStatus.NOT_FOUND)

            } else {
                responsFailure.increment()
                throw OppslagException("Feil ved oppslag på personinfo ${response?.errors?.toString()}",
                                       "PdlRestClient",
                                       OppslagException.Level.MEDIUM,
                                       HttpStatus.NOT_FOUND)
            }
        } catch (e: Exception) {
            when (e) {
                is OppslagException -> throw e
                else -> {
                    responsFailure.increment()
                    throw OppslagException(e.message, "PdlRestClient", OppslagException.Level.MEDIUM, HttpStatus.INTERNAL_SERVER_ERROR)
                }
            }
        }
    }

    private fun httpHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
        }
    }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
    }
}
