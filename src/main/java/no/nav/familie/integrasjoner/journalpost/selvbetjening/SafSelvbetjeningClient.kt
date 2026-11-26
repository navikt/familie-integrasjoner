package no.nav.familie.integrasjoner.journalpost.selvbetjening

import com.expediagroup.graphql.client.spring.GraphQLWebClient
import com.expediagroup.graphql.client.types.GraphQLClientResponse
import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.client.rest.RegoppslagRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.config.lagJettyHttpClient
import no.nav.familie.integrasjoner.config.medTimeout
import no.nav.familie.integrasjoner.safselvbetjening.generated.HentDokumentoversikt
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Tema
import no.nav.familie.integrasjoner.safselvbetjening.generated.enums.Variantformat
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Duration

@Service
class SafSelvbetjeningClient(
    @Qualifier("SafSelvbetjening") private val safSelvbetjeningGraphQLClient: GraphQLWebClient,
    @Qualifier("safSelvbetjeningTokenXRestClient") restClient: RestClient,
    @Value("\${SAF_SELVBETJENING_URL}") private val safSelvbetjeningURI: URI,
) : DisposableBean {
    private val httpClient = lagJettyHttpClient(HENT_DOKUMENT_TIMEOUT)
    private val restClient = restClient.medTimeout(httpClient, HENT_DOKUMENT_TIMEOUT)

    override fun destroy() {
        httpClient.stop()
    }

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
            return restClient
                .get()
                .uri(safHentdokumentUri)
                .accept(MediaType.APPLICATION_PDF)
                .retrieve()
                .body<ByteArray>()!!
        } catch (e: Exception) {
            throw SafSelvbetjeningException(
                "Ukjent feil ved henting av dokument. ${e.message}",
                e,
            )
        }
    }

    companion object {
        // Store PDF-er bruker ofte mer enn 10s, se RestClientTimeout.kt.
        private val HENT_DOKUMENT_TIMEOUT = Duration.ofSeconds(60)
    }
}
