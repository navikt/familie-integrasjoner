package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.personopplysning.internal.*
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
    private val graphQL = this::class.java.getResource("/pdl/hentperson.graphql").readText().graphqlCompatible()

    fun hentPerson(personIdent: String, tema: String): Person {
        val pdlPersonRequest = PdlPersonRequest(variables = PdlRequestVariable(personIdent),
                                                query = graphQL)
        try {
            val response = postForEntity<PdlHentPersonResponse>(pdlUri,
                                                                pdlPersonRequest,
                                                                httpHeaders(tema))
            if (response != null && !response.harFeil()) {
                return Result.runCatching {
                    response?.data?.person!!.let {
                        Person(fødselsdato = it.foedsel!!.first().foedselsdato!!,
                               navn = it.navn!!.first().fulltNavn(),
                               kjønn = it.kjoenn!!.first().kjoenn.toString())
                    }
                }.fold (
                        onSuccess = {it},
                        onFailure = {
                            throw OppslagException("Fant ikke forespurte data på person $personIdent",
                                                   "PdlRestClient",
                                                   OppslagException.Level.MEDIUM,
                                                   HttpStatus.NOT_FOUND,
                                                   it)
                        }
                )
            } else {
                responsFailure.increment()
                throw OppslagException("Feil ved oppslag på person $personIdent: ${response?.errorMessages()}",
                                       "PdlRestClient",
                                       OppslagException.Level.MEDIUM,
                                       HttpStatus.INTERNAL_SERVER_ERROR)
            }
        } catch (e: Exception) {
            when (e) {
                is OppslagException -> throw e
                else -> {
                    responsFailure.increment()
                    throw OppslagException("Feil ved oppslag på person $personIdent. Gav feil: ${e.message}",
                                           "PdlRestClient",
                                           OppslagException.Level.MEDIUM,
                                           HttpStatus.INTERNAL_SERVER_ERROR,
                                           e)
                }
            }
        }
    }

    private fun httpHeaders(tema: String): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
            add("Tema", tema)
        }
    }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
    }
}
