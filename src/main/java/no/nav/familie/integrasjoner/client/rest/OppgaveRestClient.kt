package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.oppgave.domene.FinnOppgaveResponseDto
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveJsonDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class OppgaveRestClient(@Value("\${OPPGAVE_URL}") private val oppgaveBaseUrl: URI,
                        @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "oppgave") {

    override val pingUri = UriUtil.uri(oppgaveBaseUrl, PATH_PING)

    private val returnerteIngenOppgaver = Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "ingen")

    private val returnerteMerEnnEnOppgave = Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "flerEnnEn")

    fun finnOppgave(request: Oppgave): OppgaveJsonDto {
        val requestUrl = lagRequestUrlMed(request.aktorId, request.journalpostId)
        return requestOppgaveJson(requestUrl)
    }

    fun finnOppgave(oppgaveId: String): OppgaveJsonDto {
        return requestOppgaveJson(requestUrl(oppgaveId.toLong()))
    }

    fun oppdaterOppgave(dto: OppgaveJsonDto, beskrivelse: String) {
        val copy = dto.copy(beskrivelse = dto.beskrivelse + beskrivelse)
        putForEntity<String>(requestUrl(copy.id), dto, httpHeaders())
    }

    private fun lagRequestUrlMed(aktoerId: String, journalpostId: String): URI {
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                .path(PATH_OPPGAVE)
                .queryParam("aktoerId", aktoerId)
                .queryParam("tema", TEMA)
                .queryParam("oppgavetype", OPPGAVE_TYPE)
                .queryParam("journalpostId", journalpostId)
                .build()
                .toUri()
    }

    private fun requestUrl(oppgaveId: Long): URI {
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl).pathSegment(PATH_OPPGAVE, oppgaveId.toString()).build().toUri()
    }

    private fun requestOppgaveJson(requestUrl: URI): OppgaveJsonDto {
        val finnOppgaveResponseDto = getForEntity<FinnOppgaveResponseDto>(requestUrl, httpHeaders())
        if (finnOppgaveResponseDto.oppgaver.isEmpty()) {
            returnerteIngenOppgaver.increment()
            throw OppslagException("Ingen oppgaver funnet for $requestUrl",
                                   "oppgave",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.NOT_FOUND)
        }
        if (finnOppgaveResponseDto.oppgaver.size > 1) {
            returnerteMerEnnEnOppgave.increment()
        }
        return finnOppgaveResponseDto.oppgaver[0]
    }

    private fun httpHeaders(): HttpHeaders = HttpHeaders().apply {
        add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
    }

    companion object {

        private const val PATH_PING = "internal/alive"
        private const val PATH_OPPGAVE = "api/v1/oppgaver"
        private const val TEMA = "KON"
        private const val OPPGAVE_TYPE = "BEH_SAK"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }

}
