package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.safselvbetjening.generated.HentDokumentoversikt
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Tema
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Variantformat
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Service
class SafSelvbetjeningClient(
    @Qualifier("SafSelvbetjening") private val safSelvbetjeningGraphQLClient: GraphQLWebClient,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
    @Value("\${SAF_SELVBETJENING_URL}") private val safSelvbetjeningURI: URI,
) : AbstractRestClient(restTemplate, "saf.selvbetjening") {
    suspend fun hentDokumentoversiktForIdent(
        ident: String,
        tema: Tema,
    ): HentDokumentoversikt.Result {
        val response: GraphQLClientResponse<HentDokumentoversikt.Result> =
            safSelvbetjeningGraphQLClient.execute(
                HentDokumentoversikt(variables = HentDokumentoversikt.Variables(ident = ident, tema = tema)),
            )
        if (response.errors.isNullOrEmpty()) {
            return response.data ?: throw SafSelvbetjeningException("Ingen data mottatt fra SAF for ident")
        } else {
            val feilmeldinger = response.errors!!.joinToString("; ") { it.message }
            throw SafSelvbetjeningException(
                "Feil ved henting av dokumentoversikt for bruker: $feilmeldinger",
            )
        }
    }

    fun hentDokument(
        journalpostId: String,
        dokumentInfoId: String,
    ): ByteArray {
        val safHentdokumentUri =
            UriComponentsBuilder
                .fromUri(safSelvbetjeningURI)
                .path("/rest/hentdokument/$journalpostId/$dokumentInfoId/${Variantformat.ARKIV}")
                .build()
                .toUri()

        try {
            return getForEntity<ByteArray>(safHentdokumentUri, HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_PDF) })
        } catch (e: Exception) {
            throw SafSelvbetjeningException(
                "Ukjent feil ved henting av dokument. ${e.message}",
                e,
            )
        }
    }
}
