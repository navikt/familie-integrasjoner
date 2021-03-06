package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.Tema
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
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

    fun hentPerson(personIdent: String, tema: String, personInfoQuery: PersonInfoQuery): Person {

        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent),
                                                query = personInfoQuery.graphQL)
        try {
            val response = postForEntity<PdlResponse<PdlPerson>>(pdlUri,
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
                throw pdlOppslagException(feilmelding = "Feil ved oppslag på person: ${response?.errorMessages()}",
                                          personIdent = personIdent)
            }
        } catch (e: Exception) {
            when (e) {
                is OppslagException -> throw e
                else -> throw pdlOppslagException(personIdent, error = e)
            }
        }
    }

    fun hentIdenter(ident: String, gruppe: String, tema: Tema, historikk: Boolean): List<PdlIdent> {
        val pdlPersonRequest = PdlIdentRequest(variables = PdlIdentRequestVariables(ident, gruppe, historikk),
                                               query = HENT_IDENTER_QUERY)

        try {
            val response = postForEntity<PdlResponse<PdlHentIdenter>>(pdlUri,
                                                                      pdlPersonRequest,
                                                                      httpHeaders(tema.name))
            if (response.harFeil() || response.data.hentIdenter == null) {
                if (response.harNotFoundFeil()) {
                    secureLogger.info("Finner ikke ident med gruppe=$gruppe for ident=$ident i PDL")
                    throw PdlNotFoundException()
                }
                throw pdlOppslagException(feilmelding = "Feil ved oppslag på person: ${response.errorMessages()}",
                                          personIdent = ident)
            }

            return response.data.hentIdenter.identer
        } catch (e: OppslagException) {
            throw e
        } catch(e: Exception) {
            throw pdlOppslagException(ident, error = e)
        }

    }

    fun hentGjeldendeAktørId(ident: String, tema: Tema): String {
        val pdlIdenter = hentIdenter(ident, "AKTORID", tema, false)
        return pdlIdenter.firstOrNull()?.ident
                ?: throw pdlOppslagException(feilmelding = "Kunne ikke finne aktørId for personIdent=$ident i PDL. ", personIdent = ident)
    }

    fun hentGjeldendePersonident(ident: String, tema: Tema): String {
        val pdlIdenter = hentIdenter(ident, "FOLKEREGISTERIDENT", tema, false)
        return pdlIdenter.firstOrNull()?.ident
                ?: throw pdlOppslagException(feilmelding = "Kunne ikke finne personIdent for aktørId=$ident i PDL. ", personIdent = ident)
    }


    private fun httpHeaders(tema: String): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            accept = listOf(MediaType.APPLICATION_JSON)
            add("Nav-Consumer-Token", "Bearer ${stsRestClient.systemOIDCToken}")
            add("Tema", tema)
        }
    }

    private fun pdlOppslagException(personIdent: String,
                                    httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                                    error: Throwable? = null,
                                    feilmelding: String = "Feil ved oppslag på person. Gav feil: ${error?.message}")
            : OppslagException {

        responsFailure.increment()
        return OppslagException(feilmelding,
                                "PdlRestClient",
                                OppslagException.Level.MEDIUM,
                                httpStatus,
                                error,
                                personIdent)
    }

    companion object {

        private const val PATH_GRAPHQL = "graphql"
        private val HENT_IDENTER_QUERY = hentGraphqlQuery("hentIdenter")
    }
}

enum class PersonInfoQuery(val graphQL: String) {
    ENKEL(hentGraphqlQuery("hentperson-enkel")),
    MED_RELASJONER(hentGraphqlQuery("hentperson-med-relasjoner"))
}

private fun hentGraphqlQuery(pdlResource: String): String {
    return PersonInfoQuery::class.java.getResource("/pdl/$pdlResource.graphql").readText().graphqlCompatible()
}

