package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.dokdist.NullResponseException
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.UriUtil
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.net.URI

@Component
class DokdistRestClient(
    @Value("\${DOKDIST_URL}") private val dokdistUri: URI,
    @Value("\${DOKDIST_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt().tokenValue }
    val distribuerUri = UriUtil.uri(dokdistUri, PATH_DISTRIBUERJOURNALPOST)

    fun distribuerJournalpost(req: DistribuerJournalpostRequestTo): DistribuerJournalpostResponseTo? {
        val response =
            try {
                restClient
                    .post()
                    .uri(distribuerUri)
                    .body(req)
                    .retrieve()
                    .body<DistribuerJournalpostResponseTo>()
            } catch (e: Exception) {
                if (e is HttpClientErrorException.Gone) {
                    throw e
                } else {
                    throw OppslagException(
                        "Feil ved distribuering av journalpost",
                        "dokdist.distribuer.distribuerJournalpost",
                        OppslagException.Level.MEDIUM,
                        HttpStatus.INTERNAL_SERVER_ERROR,
                        e,
                    )
                }
            }

        return response ?: throw NullResponseException()
    }

    companion object {
        private const val PATH_DISTRIBUERJOURNALPOST = "rest/v1/distribuerjournalpost"
    }
}
