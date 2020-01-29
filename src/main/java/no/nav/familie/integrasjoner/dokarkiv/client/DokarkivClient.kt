package no.nav.familie.integrasjoner.dokarkiv.client

import com.fasterxml.jackson.annotation.JsonAutoDetect
import com.fasterxml.jackson.annotation.PropertyAccessor
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import io.micrometer.core.instrument.Metrics
import no.nav.familie.http.client.HttpRequestUtil
import no.nav.familie.http.sts.StsRestClient
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.log.NavHttpHeaders
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import java.io.IOException
import java.net.URI
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.TimeUnit
import javax.ws.rs.core.HttpHeaders

@Service
class DokarkivClient @Autowired constructor(@param:Value("\${DOKARKIV_V1_URL}")
                                            private val dokarkivUrl: String,
                                            @param:Value("\${CREDENTIAL_USERNAME}")
                                            private val consumer: String,
                                            @param:Autowired private val stsRestClient: StsRestClient,
                                            private val objectMapper: ObjectMapper) {

    private val opprettJournalpostResponstid =
            Metrics.timer("dokarkiv.opprett.respons.tid")
    private val opprettJournalpostSuccess =
            Metrics.counter("dokarkiv.opprett.response", "status", "success")
    private val opprettJournalpostFailure =
            Metrics.counter("dokarkiv.opprett.response", "status", "failure")
    private val ferdigstillJournalpostResponstid =
            Metrics.timer("dokarkiv.ferdigstill.respons.tid")
    private val ferdigstillJournalpostSuccess =
            Metrics.counter("dokarkiv.ferdigstill.response", "status", "success")
    private val ferdigstillJournalpostFailure =
            Metrics.counter("dokarkiv.ferdigstill.response", "status", "failure")
    private val httpClient: HttpClient = HttpClient.newHttpClient()
    fun lagJournalpost(jp: OpprettJournalpostRequest?,
                       ferdigstill: Boolean): OpprettJournalpostResponse {
        val uri =
                URI.create(String.format("%s/rest/journalpostapi/v1/journalpost?foersoekFerdigstill=%b",
                                         dokarkivUrl,
                                         ferdigstill))
        val systembrukerToken = stsRestClient.systemOIDCToken
        return try {
            val requestBody = objectMapper
                    .setVisibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
                    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsBytes(jp)
            val request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .header("Content-Type", "application/json")
                    .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), consumer)
                    .header(NavHttpHeaders.NAV_CALL_ID.asString(), MDCOperations.getCallId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $systembrukerToken")
                    .POST(HttpRequest.BodyPublishers.ofByteArray(requestBody))
                    .timeout(Duration.ofSeconds(20)) // kall tar opptil 8s i preprod.
                    .build()
            val startTime = System.nanoTime()
            val httpResponse =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            opprettJournalpostResponstid.record(System.nanoTime() - startTime,
                                                TimeUnit.NANOSECONDS)
            if (httpResponse.statusCode() == HttpStatus.OK.value() || httpResponse.statusCode() == HttpStatus.CREATED.value()) {
                opprettJournalpostSuccess.increment()
                objectMapper.readValue(httpResponse.body(),
                                       OpprettJournalpostResponse::class.java)
            } else {
                opprettJournalpostFailure.increment()
                throw RuntimeException("Feilresponse fra dokarkiv-tjenesten ${httpResponse.statusCode()} ${httpResponse.body()}")
            }
        } catch (e: IOException) {
            opprettJournalpostFailure.increment()
            throw RuntimeException("Feil ved kall mot Dokarkiv uri=$uri", e)
        } catch (e: InterruptedException) {
            opprettJournalpostFailure.increment()
            throw RuntimeException("Feil ved kall mot Dokarkiv uri=$uri", e)
        }
    }

    fun ferdigstillJournalpost(journalpostId: String, journalførendeEnhet: String?) {
        val uri = URI.create(String.format("%s/rest/journalpostapi/v1/journalpost/%s/ferdigstill",
                                           dokarkivUrl,
                                           journalpostId))
        val systembrukerToken = stsRestClient.systemOIDCToken
        try {
            val request = HttpRequest.newBuilder()
                    .uri(uri)
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .header("Content-Type", "application/json")
                    .header(NavHttpHeaders.NAV_CONSUMER_ID.asString(), consumer)
                    .header(NavHttpHeaders.NAV_CALL_ID.asString(), MDCOperations.getCallId())
                    .header(HttpHeaders.AUTHORIZATION, "Bearer $systembrukerToken")
                    .method("PATCH",
                            HttpRequest.BodyPublishers.ofString(String.format(FERDIGSTILL_JOURNALPOST_JSON,
                                                                              journalførendeEnhet)))
                    .timeout(Duration.ofSeconds(20)) // kall tar opptil 8s i preprod.
                    .build()
            val startTime = System.nanoTime()
            val httpResponse =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString())
            ferdigstillJournalpostResponstid.record(System.nanoTime() - startTime,
                                                    TimeUnit.NANOSECONDS)
            if (httpResponse.statusCode() == HttpStatus.OK.value() || httpResponse.statusCode() == HttpStatus.CREATED.value()) {
                ferdigstillJournalpostSuccess.increment()
            } else if (httpResponse.statusCode() == HttpStatus.BAD_REQUEST.value()) {
                ferdigstillJournalpostFailure.increment()
                throw KanIkkeFerdigstilleJournalpostException("Kan ikke ferdigstille journalpost " +
                                                              "$journalpostId ${httpResponse.body()}")
            } else {
                ferdigstillJournalpostFailure.increment()
                throw RuntimeException("Feilresponse ved ferdigstill av journalpost " +
                                       "${httpResponse.statusCode()} ${httpResponse.body()}")
            }
        } catch (e: IOException) {
            ferdigstillJournalpostFailure.increment()
            throw RuntimeException("Feil ved kall mot Dokarkiv uri=$uri", e)
        } catch (e: InterruptedException) {
            ferdigstillJournalpostFailure.increment()
            throw RuntimeException("Feil ved kall mot Dokarkiv uri=$uri", e)
        }
    }

    @Throws(Exception::class) fun ping() {
        val uri = URI.create(String.format("%s/isAlive", dokarkivUrl))
        val request = HttpRequestUtil.createRequest("Bearer " + stsRestClient.systemOIDCToken)
                .uri(uri)
                .build()
        val response =
                httpClient.send(request, HttpResponse.BodyHandlers.ofString())
        if (HttpStatus.OK.value() != response.statusCode()) {
            throw Exception("Feil ved ping til Dokarkiv")
        }
    }

    companion object {
        const val FERDIGSTILL_JOURNALPOST_JSON = "{\"journalfoerendeEnhet\":%s}"
    }

}
