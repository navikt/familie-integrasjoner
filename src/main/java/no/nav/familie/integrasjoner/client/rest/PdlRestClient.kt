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

    private val aktørIdQuery = this::class.java.getResource("/pdl/hentIdenter.graphql").readText().graphqlCompatible()

    fun hentPerson(personIdent: String, tema: String, personInfoQuery: PersonInfoQuery): Person {

        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent),
                                                query = personInfoQuery.graphQL)
        try {
            val response = postForEntity<PdlHentPersonResponse>(pdlUri,
                                                                pdlPersonRequest,
                                                                httpHeaders(tema))
            if (response != null && !response.harFeil()) {
                return Result.runCatching {
                    val familierelasjoner: Set<Familierelasjon> =
                            when (personInfoQuery) {
                                PersonInfoQuery.ENKEL -> emptySet()
                                PersonInfoQuery.MED_RELASJONER -> {
                                    response.data.person!!.familierelasjoner.map { relasjon ->
                                        Familierelasjon(personIdent = Personident(id = relasjon.relatertPersonsIdent),
                                                        relasjonsrolle = relasjon.relatertPersonsRolle.toString())
                                    }.toSet()
                                }
                            }
                    response.data.person!!.let {
                        Person(fødselsdato = it.foedsel.first().foedselsdato!!,
                               navn = it.navn.first().fulltNavn(),
                               kjønn = it.kjoenn.first().kjoenn.toString(),
                               familierelasjoner = familierelasjoner,
                               adressebeskyttelseGradering = it.adressebeskyttelse.firstOrNull()?.gradering,
                               bostedsadresse = it.bostedsadresse.firstOrNull(),
                               sivilstand = it.sivilstand.firstOrNull()?.type)
                    }
                }.fold(
                        onSuccess = { it },
                        onFailure = {
                            throw OppslagException("Fant ikke forespurte data på person.",
                                                   "PdlRestClient",
                                                   OppslagException.Level.MEDIUM,
                                                   HttpStatus.NOT_FOUND,
                                                   it,
                                                   personIdent)
                        }
                )
            } else {
                responsFailure.increment()
                throw OppslagException("Feil ved oppslag på person: ${response?.errorMessages()}",
                                       "PdlRestClient",
                                       OppslagException.Level.MEDIUM,
                                       HttpStatus.INTERNAL_SERVER_ERROR,
                                       null,
                                       personIdent)
            }
        } catch (e: Exception) {
            when (e) {
                is OppslagException -> throw e
                else -> {
                    responsFailure.increment()
                    throw OppslagException("Feil ved oppslag på person. Gav feil: ${e.message}",
                                           "PdlRestClient",
                                           OppslagException.Level.MEDIUM,
                                           HttpStatus.INTERNAL_SERVER_ERROR,
                                           e,
                                           personIdent)
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

    fun hentIdenter(personIdent: String, tema: String): PdlHentIdenterResponse {
        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent),
                                                query = aktørIdQuery)
        val response = postForEntity<PdlHentIdenterResponse>(pdlUri,
                                                             pdlPersonRequest,
                                                             httpHeaders(tema))


        if (response != null && !response.harFeil()) {
            return response
        }
        throw OppslagException("Fant ikke identer for person: " + response?.errorMessages(),
                               "PdlRestClient",
                               OppslagException.Level.MEDIUM,
                               HttpStatus.NOT_FOUND,
                               null,
                               personIdent)
    }

    companion object {
        private const val PATH_GRAPHQL = "graphql"
    }
}

enum class PersonInfoQuery(val graphQL: String) {
    ENKEL(this::class.java.getResource("/pdl/hentperson-enkel.graphql").readText().graphqlCompatible()),
    MED_RELASJONER(this::class.java.getResource("/pdl/hentperson-med-relasjoner.graphql").readText().graphqlCompatible())
}
