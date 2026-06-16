package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.integrasjoner.dokdist.NullResponseException
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.Pingable
import no.nav.familie.integrasjoner.felles.UriUtil
import org.springframework.beans.factory.annotation.Qualifier
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
    @Qualifier("dokdistRestClient") private val restClient: RestClient,
) : Pingable {
    override val pingUri: URI = UriUtil.uri(dokdistUri, PATH_PING)
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

    override fun ping(): String =
        restClient
            .get()
            .uri(pingUri)
            .retrieve()
            .body<String>() ?: "OK"

    companion object {
        private const val PATH_PING = "internal/alive"
        private const val PATH_DISTRIBUERJOURNALPOST = "rest/v1/distribuerjournalpost"
    }
}
