package no.nav.familie.integrasjoner.client.rest

import com.fasterxml.jackson.core.JsonProcessingException
import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.ks.kontrakter.oppgave.Oppgave
import no.nav.familie.log.mdc.MDCConstants
import no.nav.oppgave.v1.FinnOppgaveResponseDto
import no.nav.oppgave.v1.OppgaveJsonDto
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI
import java.util.*

@Component
class OppgaveClient(@Value("\${OPPGAVE_URL}") val url: String,
                    @Qualifier("sts") val restTemplate: RestOperations) {

    private val returnerteIngenOppgaver =
            Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "ingen")
    private val returnerteMerEnnEnOppgave =
            Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "flerEnnEn")
    private val oppgaveUri: URI
    private val oppgaveBaseUrl: String
    fun finnOppgave(request: Oppgave): OppgaveJsonDto {
        val requestUrl = lagRequestUrlMed(oppgaveUri, request.aktorId, request.journalpostId)
        return requestOppgaveJson(requestUrl)
    }

    fun finnOppgave(oppgaveId: String): OppgaveJsonDto {
        val requestUrl = URI.create("$oppgaveUri/$oppgaveId")
        return requestOppgaveJson(requestUrl)
    }

    fun oppdaterOppgave(dto: OppgaveJsonDto, beskrivelse: String) {
        dto.beskrivelse = dto.beskrivelse + beskrivelse
        try {
            putRequest(URI.create(oppgaveUri.toString() + "/" + dto.id),
                       objectMapper.writeValueAsString(dto),
                       String::class.java)
        } catch (e: JsonProcessingException) {
            throw RuntimeException("Mapping av OppgaveJsonDto til String feilet.", e)
        }
    }

    fun ping() {
        getRequest(URI.create(String.format("%s/client/alive", oppgaveBaseUrl)),
                   String::class.java)
    }

    private fun lagRequestUrlMed(oppgaveUri: URI, aktoerId: String, journalpostId: String): URI {
        return URI.create(oppgaveUri.toString() + String.format("?aktoerId=%s&tema=%s&oppgavetype=%s&journalpostId=%s",
                                                                aktoerId,
                                                                TEMA,
                                                                OPPGAVE_TYPE,
                                                                journalpostId))
    }

    private fun requestOppgaveJson(requestUrl: URI): OppgaveJsonDto {
        val response =
                getRequest(requestUrl, FinnOppgaveResponseDto::class.java)
        if (Objects.requireNonNull(response.body).oppgaver.isEmpty()) {
            returnerteIngenOppgaver.increment()
            throw OppslagException("Ingen oppgaver funnet for $requestUrl",
                                   "oppgave",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.NOT_FOUND)
        }
        if (response.body.oppgaver.size > 1) {
            returnerteMerEnnEnOppgave.increment()
        }
        return response.body.oppgaver[0]
    }

    private fun <T> getRequest(uri: URI, responseType: Class<T>): ResponseEntity<T> {
        val headers = HttpHeaders()
        headers.add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
        return restTemplate.exchange(uri,
                                     HttpMethod.GET,
                                     HttpEntity<Any>(headers),
                                     responseType)
    }

    private fun <T> putRequest(uri: URI, requestBody: String, responseType: Class<T>) {
        val headers = HttpHeaders()
        headers.add("Content-Type", "application/json;charset=UTF-8")
        headers.add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
        restTemplate.exchange(uri,
                              HttpMethod.PUT,
                              HttpEntity(requestBody, headers),
                              responseType)
    }

    companion object {
        private const val TEMA = "KON"
        private const val OPPGAVE_TYPE = "BEH_SAK"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }

    init {
        oppgaveUri = URI.create("$url/api/v1/oppgaver")
        oppgaveBaseUrl = url
    }
}