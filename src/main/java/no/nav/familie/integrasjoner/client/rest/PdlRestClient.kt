package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.UriUtil
import no.nav.familie.integrasjoner.felles.graphqlQuery
import no.nav.familie.integrasjoner.geografisktilknytning.GeografiskTilknytningDto
import no.nav.familie.integrasjoner.geografisktilknytning.PdlGeografiskTilknytningRequest
import no.nav.familie.integrasjoner.geografisktilknytning.PdlGeografiskTilknytningVariables
import no.nav.familie.integrasjoner.geografisktilknytning.PdlHentGeografiskTilknytning
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.integrasjoner.personopplysning.PdlUnauthorizedException
import no.nav.familie.integrasjoner.personopplysning.internal.PdlAdressebeskyttelse
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
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.kontrakter.felles.Tema
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Service
class PdlRestClient(
    @Value("\${PDL_URL}") pdlBaseUrl: URI,
    @Value("\${PDL_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt()?.tokenValue }
    private val pdlUri = UriUtil.uri(pdlBaseUrl, PATH_GRAPHQL)
    private val logger = LoggerFactory.getLogger(PdlRestClient::class.java)
    private val secureLogger = LoggerFactory.getLogger("secureLogger")
    private val responsFailure = Metrics.counter("restclient.response.failure", "client", "pdl.personinfo")

    fun hentAdressebeskyttelse(
        personIdent: String,
        tema: Tema,
    ): PdlAdressebeskyttelse {
        val pdlAdressebeskyttelseRequest =
            PdlPersonRequest(
                variables = PdlPersonRequestVariables(personIdent),
                query = HENT_ADRESSEBESKYTTELSE_QUERY,
            )

        val response: PdlResponse<PdlPersonMedAdressebeskyttelse> =
            try {
                restClient
                    .post()
                    .uri(pdlUri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("Tema", tema.name)
                    .header("behandlingsnummer", tema.behandlingsnummer)
                    .body(pdlAdressebeskyttelseRequest)
                    .retrieve()
                    .body<PdlResponse<PdlPersonMedAdressebeskyttelse>>()!!
            } catch (e: Exception) {
                throw pdlOppslagException(
                    personIdent = personIdent,
                    httpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
                    error = e,
                    feilmelding = "Feil ved henting av adressebeskyttelse",
                    kilde = "PdlRestClient.hentAdressebeskyttelse",
                )
            }

        return feilsjekkOgReturnerData(response, personIdent, kilde = "PdlRestClient.hentAdressebeskyttelse") { it.person }
    }

    fun hentPerson(
        personIdent: String,
        tema: Tema,
    ): Person {
        val pdlPersonRequest =
            PdlPersonRequest(
                variables = PdlPersonRequestVariables(personIdent),
                query = HENT_PERSON,
            )
        val response =
            try {
                restClient
                    .post()
                    .uri(pdlUri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("Tema", tema.name)
                    .header("behandlingsnummer", tema.behandlingsnummer)
                    .body(pdlPersonRequest)
                    .retrieve()
                    .body<PdlResponse<PdlPerson>>()!!
            } catch (e: Exception) {
                throw pdlOppslagException(personIdent, error = e, kilde = "PdlRestClient.hentPerson")
            }
        return feilsjekkOgReturnerData(response, personIdent, kilde = "PdlRestClient.hentPerson") { it.person }.let {
            Person(
                navn = it.navn.first().fulltNavn(),
                adressebeskyttelseGradering = it.adressebeskyttelse.firstOrNull()?.gradering,
            )
        }
    }

    fun hentIdenter(
        ident: String,
        gruppe: String,
        tema: Tema,
        historikk: Boolean,
    ): List<PdlIdent> {
        val pdlPersonRequest =
            PdlIdentRequest(
                variables = PdlIdentRequestVariables(ident, gruppe, historikk),
                query = HENT_IDENTER_QUERY,
            )

        val response =
            try {
                restClient
                    .post()
                    .uri(pdlUri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("Tema", tema.name)
                    .header("behandlingsnummer", tema.behandlingsnummer)
                    .body(pdlPersonRequest)
                    .retrieve()
                    .body<PdlResponse<PdlHentIdenter>>()!!
            } catch (e: Exception) {
                throw pdlOppslagException(ident, error = e, kilde = "PdlRestClient.hentIdenter")
            }
        return feilsjekkOgReturnerData(response, ident, kilde = "PdlRestClient.hentIdenter") { it.hentIdenter }.identer
    }

    private inline fun <reified DATA : Any, reified RESPONSE : Any> feilsjekkOgReturnerData(
        pdlResponse: PdlResponse<DATA>,
        personIdent: String,
        kilde: String,
        dataMapper: (DATA) -> RESPONSE?,
    ): RESPONSE {
        if (pdlResponse.harFeil()) {
            if (pdlResponse.harNotFoundFeil()) {
                secureLogger.info("Finner ikke person for ident=$personIdent i PDL")
                throw PdlNotFoundException()
            }
            if (pdlResponse.harUnauthorizedFeil()) {
                secureLogger.info("Har ikke tilgang til person med ident=$personIdent i PDL")
                val pdlUnauthorizedDetails = pdlResponse.tilPdlUnauthorizedDetails().first()
                throw PdlUnauthorizedException(melding = pdlUnauthorizedDetails.cause ?: pdlUnauthorizedDetails.policy)
            }
            throw pdlOppslagException(
                feilmelding = "Feil ved oppslag på person: ${pdlResponse.errorMessages()}. Se secureLogs for mer info.",
                personIdent = personIdent,
                kilde = kilde,
            )
        }
        if (pdlResponse.harAdvarsel()) {
            logger.warn("Advarsel ved henting av ${DATA::class} fra PDL. Se securelogs for detaljer.")
            secureLogger.warn("Advarsel ved henting av ${DATA::class} fra PDL: ${pdlResponse.extensions?.warnings}")
        }
        val data =
            dataMapper.invoke(pdlResponse.data)
                ?: throw pdlOppslagException(
                    feilmelding = "Feil ved oppslag på person. Objekt mangler på responsen fra PDL. Se secureLogs for mer info.",
                    personIdent = personIdent,
                    kilde = kilde,
                )
        return data
    }

    fun hentGjeldendePersonident(
        ident: String,
        tema: Tema,
    ): String {
        val pdlIdenter = hentIdenter(ident, "FOLKEREGISTERIDENT", tema, false)
        return pdlIdenter.firstOrNull()?.ident
            ?: throw pdlOppslagException(
                feilmelding = "Kunne ikke finne personIdent i PDL. Se secureLogs for mer info. ",
                personIdent = ident,
                kilde = "PdlRestClient.hentGjeldendePersonident",
            )
    }

    fun hentGeografiskTilknytning(
        personIdent: String,
        tema: Tema,
    ): GeografiskTilknytningDto {
        val pdlGeografiskTilknytningRequest =
            PdlGeografiskTilknytningRequest(
                variables = PdlGeografiskTilknytningVariables(personIdent),
                query = HENT_GEOGRAFISK_TILKNYTNING_QUERY,
            )
        try {
            val response: PdlResponse<PdlHentGeografiskTilknytning> =
                restClient
                    .post()
                    .uri(pdlUri)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .header(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                    .header("Tema", tema.name)
                    .header("behandlingsnummer", tema.behandlingsnummer)
                    .body(pdlGeografiskTilknytningRequest)
                    .retrieve()
                    .body<PdlResponse<PdlHentGeografiskTilknytning>>()!!

            if (response.harFeil()) {
                if (response.harNotFoundFeil()) {
                    secureLogger.info("Finner ikke geografisk tilknytning for ident=$personIdent i PDL")
                    throw PdlNotFoundException()
                }
                if (response.harUnauthorizedFeil()) {
                    secureLogger.info("Har ikke tilgang til å hente geografisk tilknytning for ident=$personIdent i PDL. Ekstra info: ${response.errors?.joinToString { it.extensions.toString() } ?: "Ingen detaljer"}")
                    secureLogger.error("Ikke tilgang til oppslag på geografisk tilknytning på person. Feilmelding fra PDL: ${response.errorMessages()}")
                    val pdlUnauthorizedDetails = response.tilPdlUnauthorizedDetails().first()
                    throw PdlUnauthorizedException(melding = pdlUnauthorizedDetails.cause ?: pdlUnauthorizedDetails.policy)
                }
                throw pdlOppslagException(
                    feilmelding = "Feil ved oppslag på geografisk tilknytning på person: ${response.errorMessages()}",
                    personIdent = personIdent,
                    kilde = "PdlRestClient.hentGeografiskTilknytning",
                )
            }
            if (response.data.hentGeografiskTilknytning == null) {
                secureLogger.info("Finner ikke geografisk tilknytning for ident=$personIdent i PDL")
                throw PdlNotFoundException()
            }
            return response.data.hentGeografiskTilknytning
        } catch (e: Exception) {
            when (e) {
                is OppslagException -> throw e
                is PdlNotFoundException -> throw e
                else -> throw pdlOppslagException(personIdent, error = e, kilde = "PdlRestClient.hentGeografiskTilknytning")
            }
        }
    }

    private fun pdlOppslagException(
        personIdent: String,
        httpStatus: HttpStatus = HttpStatus.INTERNAL_SERVER_ERROR,
        error: Throwable? = null,
        feilmelding: String = "Feil ved oppslag på person. Gav feil: ${error?.message}",
        kilde: String,
    ): OppslagException {
        responsFailure.increment()
        return OppslagException(
            feilmelding,
            kilde,
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
    }
}
