package no.nav.familie.integrasjoner.client.rest

import com.fasterxml.jackson.core.JsonProcessingException
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.journalpost.JournalpostRequestParserException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.internal.Journalpost
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostRequest
import no.nav.familie.integrasjoner.journalpost.internal.SafJournalpostResponse
import no.nav.familie.integrasjoner.journalpost.internal.SafRequestVariable
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestOperations
import java.net.URI
import java.time.Duration
import java.util.concurrent.TimeUnit

@Service
class SafKlient(@Value("\${SAF_URL}") safUrl: String?,
                @Qualifier("sts") val restTemplate: RestOperations) {

    private val hentJournalpostResponstid =
            Metrics.timer("saf.journalpost.tid")
    private val hentJournalpostResponsSuccess =
            Metrics.counter("saf.journalpost.response", "status", "success")
    private val hentJournalpostResponsFailure =
            Metrics.counter("saf.journalpost.response", "status", "failure")
    private val safUri: URI
    private val safBaseUrl: String?
    fun hentJournalpost(journalpostId: String): Journalpost {
        val safJournalpostRequest = SafJournalpostRequest(SafRequestVariable(journalpostId))
        val requestBody = convertRequestToJsonString(journalpostId, safJournalpostRequest)
        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_JSON
        headers.add(NAV_CALL_ID, MDCOperations.getCallId())
        val request =
                HttpEntity(requestBody, headers)
        val startTime = System.nanoTime()
        return try {
            val response = restTemplate.exchange(
                    safUri,
                    HttpMethod.POST,
                    request,
                    SafJournalpostResponse::class.java)
            hentJournalpostResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            if (response.body != null && !response.body!!.harFeil()) {
                hentJournalpostResponsSuccess.increment()
                response.body!!.data.journalpost
            } else {
                throw JournalpostRestClientException("Kan ikke hente journalpost " + response.body!!.errors.toString(),
                                                     null,
                                                     journalpostId)
            }
        } catch (e: RestClientException) {
            hentJournalpostResponsFailure.increment()
            throw JournalpostRestClientException(e.message, e, journalpostId)
        }
    }

    fun ping() {
        val headers = HttpHeaders()
        restTemplate.getForEntity(String.format("%s/isAlive", safBaseUrl),
                              String::class.java)
    }

    private fun convertRequestToJsonString(journalpostId: String,
                                           safJournalpostRequest: SafJournalpostRequest): String {
        return try {
            objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(safJournalpostRequest)
        } catch (e: JsonProcessingException) {
            hentJournalpostResponsFailure.increment()
            throw JournalpostRequestParserException("Parsing av request mot saf feilet for journalpostId=$journalpostId")
        }
    }

    companion object {
        private const val NAV_CALL_ID = "nav-callid"
    }

    init {
        this.restTemplate = RestTemplateBuilder()
                .configure(restTemplate)
        safUri = URI.create(String.format("%s/graphql", safUrl))
        safBaseUrl = safUrl
    }
}