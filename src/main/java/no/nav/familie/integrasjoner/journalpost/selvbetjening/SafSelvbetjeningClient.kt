package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.OppslagException.Level
import no.nav.familie.integrasjoner.journalpost.graphql.HentDokumentoversikt
import no.nav.familie.integrasjoner.journalpost.graphql.enums.Tema
import no.nav.familie.integrasjoner.journalpost.graphql.hentdokumentoversikt.Dokumentoversikt
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
    @Qualifier("SafSelvbetjening") private val graphQLWebClient: GraphQLWebClient,
    @Qualifier("jwtBearer") private val restTemplate: RestOperations,
    @Value("\${SAF_SELVBETJENING_URL}") private val safSelvbetjeningURI: URI,
) : AbstractRestClient(restTemplate, "saf.selvbetjening") {
    suspend fun hentDokumentoversiktForIdent(
        ident: String,
        tema: Tema,
    ): Dokumentoversikt {
        val response =
            graphQLWebClient.execute(
                HentDokumentoversikt(variables = HentDokumentoversikt.Variables(ident = ident, tema = tema)),
            )
        if (response.errors.isNullOrEmpty()) {
            return response.data!!.dokumentoversiktSelvbetjening
        } else {
            throw OppslagException(
                "Feil ved henting av dokumentoversikt for bruker: ${
                    response.errors!!.joinToString(", ") { it.message }
                }",
                "saf.selvbetjening.hentDokumentoversiktForIdent",
                Level.MEDIUM,
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
                .path("/rest/hentdokument/$journalpostId/$dokumentInfoId/ARKIV")
                .build()
                .toUri()

        return getForEntity<ByteArray>(safHentdokumentUri, HttpHeaders().apply { accept = listOf(MediaType.APPLICATION_PDF) })
    }
}
