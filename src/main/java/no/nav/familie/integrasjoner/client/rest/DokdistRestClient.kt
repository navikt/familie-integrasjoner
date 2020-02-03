package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.dokdist.KanIkkeDistribuereJournalpostException
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostRequestTo
import no.nav.familie.integrasjoner.dokdist.domene.DistribuerJournalpostResponseTo
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import java.net.URI

@Component
class DokdistRestClient(@Value("\${DOKDIST_URL}") private val dokdistUri: URI,
                        @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "dokdist") {

    override val pingUri: URI = UriUtil.uri(dokdistUri, PATH_PING)

    val distribuerUri = UriUtil.uri(dokdistUri, PATH_DISTRIBUERJOURNALPOST)

    fun distribuerJournalpost(req: DistribuerJournalpostRequestTo): DistribuerJournalpostResponseTo? {
        try {
            return postForEntity(distribuerUri, req)
        } catch (e: RestClientResponseException) {
            if (e.rawStatusCode == HttpStatus.BAD_REQUEST.value()) {
                val message = e.responseBodyAsString.substringAfter("\"message\": \"").substringBefore("\",\n")
                throw KanIkkeDistribuereJournalpostException("Dokdist svarte med:\n" + message)
            }
            throw e
        }
    }

    companion object {
        private const val PATH_PING = "internal/isAlive"
        private const val PATH_DISTRIBUERJOURNALPOST = "rest/v1/distribuerjournalpost"
    }
}
