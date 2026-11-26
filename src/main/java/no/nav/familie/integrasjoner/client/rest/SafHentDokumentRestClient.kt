package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.config.incrementLoggFeil
import no.nav.familie.integrasjoner.config.lagJettyHttpClient
import no.nav.familie.integrasjoner.config.medTimeout
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import org.springframework.beans.factory.DisposableBean
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import java.time.Duration

/**
 * Henting av dokumenter krever saksbehandler context.
 * Bruk av denne clienten vil fungere med et azure systemtoken,
 * men vil stange i saf sin implementasjon mot abac.
 */
@Service
class SafHentDokumentRestClient(
    @Value("\${SAF_URL}") safBaseUrl: URI,
    @Value("\${SAF_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) : DisposableBean {
    private val httpClient = lagJettyHttpClient(HENT_DOKUMENT_TIMEOUT)
    private val restClient: RestClient =
        entraIDRestClientFactory
            .lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt().tokenValue }
            .medTimeout(httpClient, HENT_DOKUMENT_TIMEOUT)
    private val safHentdokumentUri = UriComponentsBuilder.fromUri(safBaseUrl).path(PATH_HENT_DOKUMENT)

    override fun destroy() {
        httpClient.stop()
    }

    fun hentDokument(
        journalpostId: String,
        dokumentInfoId: String,
        variantFormat: String,
    ): ByteArray {
        val hentDokumentUri = safHentdokumentUri.buildAndExpand(journalpostId, dokumentInfoId, variantFormat).toUri()
        try {
            return restClient
                .get()
                .uri(hentDokumentUri)
                .accept(MediaType.ALL)
                .header(NAV_CALL_ID, MDCOperations.getCallId())
                .retrieve()
                .body<ByteArray>()!!
        } catch (e: HttpClientErrorException.Forbidden) {
            incrementLoggFeil("saf.dokument.forbidden")
            throw JournalpostForbiddenException(e.message, e)
        } catch (e: Exception) {
            incrementLoggFeil("saf.dokument")
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    companion object {
        // Store PDF-er bruker ofte mer enn 10s, se RestClientTimeout.kt.
        private val HENT_DOKUMENT_TIMEOUT = Duration.ofSeconds(60)
        private const val PATH_HENT_DOKUMENT = "/rest/hentdokument/{journalpostId}/{dokumentInfoId}/{variantFormat}"
        private const val NAV_CALL_ID = "Nav-Callid"
    }
}
