package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.graphqlCompatible
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.PdlGeografiskTilknytningRequest
import no.nav.familie.integrasjoner.geografisktilknytning.PdlGeografiskTilknytningVariables
import no.nav.familie.integrasjoner.geografisktilknytning.PdlHentGeografiskTilknytning
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.integrasjoner.personopplysning.internal.Familierelasjon
import no.nav.familie.integrasjoner.personopplysning.internal.PdlHentIdenter
import no.nav.familie.integrasjoner.personopplysning.internal.PdlIdent
import no.nav.familie.integrasjoner.personopplysning.internal.PdlIdentRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlIdentRequestVariables
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPerson
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonMedAdressebeskyttelse
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonRequest
import no.nav.familie.integrasjoner.personopplysning.internal.PdlPersonRequestVariables
import no.nav.familie.integrasjoner.personopplysning.internal.PdlResponse
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.familie.integrasjoner.personopplysning.internal.Personident
import no.nav.familie.kontrakter.felles.Tema
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
                    @Qualifier("jwtBearer") private val restTemplate: RestOperations)
    : AbstractRestClient(restTemplate, "pdl.personinfo") {

    private val pdlUri = UriUtil.uri(pdlBaseUrl, PATH_GRAPHQL)

    fun hentAdressebeskyttelse(personIdent: String, tema: Tema): PdlPersonMedAdressebeskyttelse {
        val pdlAdressebeskyttelseRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent),
                                                            query = HENT_ADRESSEBESKYTTELSE_QUERY)

        val response: PdlResponse<PdlPersonMedAdressebeskyttelse> = postForEntity(pdlUri,
                                                                                  pdlAdressebeskyttelseRequest,
                                                                                  pdlHttpHeaders(tema))

        return feilsjekkOgReturnerData(response, personIdent)
    }

    fun hentPerson(personIdent: String, tema: Tema, personInfoQuery: PersonInfoQuery): Person {

        val pdlPersonRequest = PdlPersonRequest(variables = PdlPersonRequestVariables(personIdent),
                                                query = personInfoQuery.graphQL)
        try {
            val response = postForEntity<PdlResponse<PdlPerson>>(pdlUri,
                                                                 pdlPersonRequest,
                                                                 pdlHttpHeaders(tema))
            if (!response.harFeil()) {
                return Result.runCatching {
                    val familierelasjoner: Set<Familierelasjon> =
                            when (personInfoQuery) {
                                PersonInfoQuery.ENKEL -> emptySet()
                                PersonInfoQuery.MED_RELASJONER -> {
                                    response.data.person!!.forelderBarnRelasjon.map { relasjon ->
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
                throw pdlOppslagException(feilmelding = "Feil ved oppslag på person: ${response.errorMessages()}",
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
                                                                      pdlHttpHeaders(tema))
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
        } catch (e: Exception) {
            throw pdlOppslagException(ident, error = e)
        }
    }

    private inline fun <reified T : Any> feilsjekkOgReturnerData(pdlResponse: PdlResponse<T>, personIdent: String): T {
        if (!pdlResponse.harFeil()) {
            return pdlResponse.data
        } else {
            throw pdlOppslagException(feilmelding = "Feil ved oppslag på person: ${pdlResponse.errorMessages()}",
                                      personIdent = personIdent)
        }
    }

    fun hentGjeldendeAktørId(ident: String, tema: Tema): String {
        val pdlIdenter = hentIdenter(ident, "AKTORID", tema, false)
        return pdlIdenter.firstOrNull()?.ident
               ?: throw pdlOppslagException(feilmelding = "Kunne ikke finne aktørId for personIdent=$ident i PDL. ",
                                            personIdent = ident)
    }

    fun hentGjeldendePersonident(ident: String, tema: Tema): String {
        val pdlIdenter = hentIdenter(ident, "FOLKEREGISTERIDENT", tema, false)
        return pdlIdenter.firstOrNull()?.ident
               ?: throw pdlOppslagException(feilmelding = "Kunne ikke finne personIdent for aktørId=$ident i PDL. ",
                                            personIdent = ident)
    }

    fun hentGeografiskTilknytning(personIdent: String, tema: Tema): GeografiskTilknytningDto {
        val pdlGeografiskTilknytningRequest =
                PdlGeografiskTilknytningRequest(variables = PdlGeografiskTilknytningVariables(personIdent),
                                                query = HENT_GEOGRAFISK_TILKNYTNING_QUERY)
        try {
            val response: PdlResponse<PdlHentGeografiskTilknytning> = postForEntity(pdlUri,
                                                                                    pdlGeografiskTilknytningRequest,
                                                                                    pdlHttpHeaders(tema))

            if (response.harFeil()) {
                if (response.harNotFoundFeil()) {
                    secureLogger.info("Finner ikke geografisk tilknytning for ident=$personIdent i PDL")
                    throw PdlNotFoundException()
                }
                throw pdlOppslagException(feilmelding = "Feil ved oppslag på geografisk tilknytning på person: " +
                                                        response.errorMessages(),
                                          personIdent = personIdent)
            }
            return response.data.hentGeografiskTilknytning ?: throw PdlNotFoundException()
        } catch (e: Exception) {
            when (e) {
                is OppslagException -> throw e
                is PdlNotFoundException -> throw e
                else -> throw pdlOppslagException(personIdent, error = e)
            }
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
        private val HENT_IDENTER_QUERY = hentPdlGraphqlQuery("hentIdenter")
        private val HENT_GEOGRAFISK_TILKNYTNING_QUERY = graphqlQuery("/pdl/geografisk_tilknytning.graphql")
        private val HENT_ADRESSEBESKYTTELSE_QUERY = graphqlQuery("/pdl/adressebeskyttelse.graphql")
        private val HENT_PERSON_RELASJONER_ADRESSEBESKYTTELSE = hentPdlGraphqlQuery("hentpersoner-relasjoner-adressebeskyttelse")
    }
}

enum class PersonInfoQuery(val graphQL: String) {
    ENKEL(hentPdlGraphqlQuery("hentperson-enkel")),
    MED_RELASJONER(hentPdlGraphqlQuery("hentperson-med-relasjoner"))
}

fun hentPdlGraphqlQuery(pdlResource: String): String {
    return PersonInfoQuery::class.java.getResource("/pdl/$pdlResource.graphql").readText().graphqlCompatible()
}

fun pdlHttpHeaders(tema: Tema): HttpHeaders {
    return HttpHeaders().apply {
        contentType = MediaType.APPLICATION_JSON
        accept = listOf(MediaType.APPLICATION_JSON)
        add("Tema", tema.name)
    }
}

