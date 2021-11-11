package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.oppgave.DeprecatedFinnOppgaveResponseDto
import no.nav.familie.integrasjoner.oppgave.DeprecatedOppgave
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveRequest
import no.nav.familie.integrasjoner.oppgave.domene.limitMotOppgave
import no.nav.familie.integrasjoner.oppgave.domene.toDto
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.log.mdc.MDCConstants
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI
import kotlin.math.min

@Component
class OppgaveRestClient(@Value("\${OPPGAVE_URL}") private val oppgaveBaseUrl: URI,
                        @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "oppgave") {

    override val pingUri = UriUtil.uri(oppgaveBaseUrl, PATH_PING)

    private val returnerteIngenOppgaver = Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "ingen")
    private val returnerteMerEnnEnOppgave = Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "flerEnnEn")

    private val logger = LoggerFactory.getLogger(OppgaveRestClient::class.java)

    fun finnÅpenBehandleSakOppgave(request: Oppgave): Oppgave {
        request.takeUnless { it.aktoerId == null } ?: error("Finner ikke aktør id på request")
        request.takeUnless {
            it.journalpostId == null
        } ?: error("Finner ikke journalpost id på request")

        val requestUrl = lagRequestUrlMed(request.aktoerId!!,
                                          request.journalpostId!!, request.tema?.name ?: KONTANTSTØTTE_TEMA.name)
        return requestOppgaveJson(requestUrl)
    }

    fun finnOppgaveMedId(oppgaveId: Long): Oppgave {
        return getForEntity(requestUrl(oppgaveId), httpHeaders())
    }

    @Deprecated("Bruk finnOppgaver")
    fun finnOppgaverV3(finnOppgaveRequest: FinnOppgaveRequest): DeprecatedFinnOppgaveResponseDto {

        val oppgaveRequest = finnOppgaveRequest.toDto()
        var offset = oppgaveRequest.offset

        val oppgaverOgAntall =
                getForEntity<DeprecatedFinnOppgaveResponseDto>(buildOppgaveRequestUri(oppgaveRequest), httpHeaders())
        val oppgaver: MutableList<DeprecatedOppgave> = oppgaverOgAntall.oppgaver.toMutableList()
        val grense =
                if (finnOppgaveRequest.limit == null) oppgaverOgAntall.antallTreffTotalt
                else oppgaveRequest.offset + finnOppgaveRequest.limit!!
        offset += limitMotOppgave

        while (offset < grense) {
            val nyeOppgaver: DeprecatedFinnOppgaveResponseDto =
                    getForEntity(buildOppgaveRequestUri(oppgaveRequest.copy(offset = offset,
                                                                            limit = min((grense - offset), limitMotOppgave))),
                                 httpHeaders())
            oppgaver.addAll(nyeOppgaver.oppgaver)
            offset += limitMotOppgave
        }
        return DeprecatedFinnOppgaveResponseDto(oppgaverOgAntall.antallTreffTotalt, oppgaver)
    }

    fun buildOppgaveRequestUri(oppgaveRequest: OppgaveRequest): URI =
            UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                    .path(PATH_OPPGAVE)
                    .queryParams(oppgaveRequest.toQueryParams())
                    .build()
                    .toUri()

    fun buildMappeRequestUri(mappeRequest: FinnMappeRequest) =
            UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                    .path(PATH_MAPPE)
                    .queryParams(mappeRequest.toQueryParams())
                    .build()
                    .toUri()


    fun finnOppgaver(finnOppgaveRequest: FinnOppgaveRequest): FinnOppgaveResponseDto {

        val oppgaveRequest = finnOppgaveRequest.toDto()
        var offset = oppgaveRequest.offset

        val oppgaverOgAntall =
                getForEntity<FinnOppgaveResponseDto>(buildOppgaveRequestUri(oppgaveRequest), httpHeaders())
        val oppgaver: MutableList<Oppgave> = oppgaverOgAntall.oppgaver.toMutableList()
        val grense =
                if (finnOppgaveRequest.limit == null) oppgaverOgAntall.antallTreffTotalt
                else oppgaveRequest.offset + finnOppgaveRequest.limit!!
        offset += limitMotOppgave

        while (offset < grense) {
            val nyeOppgaver =
                    getForEntity<FinnOppgaveResponseDto>(buildOppgaveRequestUri(oppgaveRequest
                                                                                        .copy(offset = offset,
                                                                                              limit = min((grense - offset),
                                                                                                          limitMotOppgave))),
                                                         httpHeaders())
            oppgaver.addAll(nyeOppgaver.oppgaver)
            offset += limitMotOppgave
        }

        return FinnOppgaveResponseDto(oppgaverOgAntall.antallTreffTotalt, oppgaver)
    }

    fun finnMapper(finnMappeRequest: FinnMappeRequest): FinnMappeResponseDto {
        return getForEntity(buildMappeRequestUri(finnMappeRequest), httpHeaders())
    }

    fun oppdaterOppgave(patchDto: Oppgave): Oppgave? {
        return Result.runCatching {
            patchForEntity<Oppgave>(requestUrl(patchDto.id ?: error("Kan ikke finne oppgaveId på oppgaven")),
                                    patchDto,
                                    httpHeaders())
        }.fold(
                onSuccess = { it },
                onFailure = {
                    var feilmelding = "Feil ved oppdatering av oppgave for ${patchDto.id}."
                    if (it is HttpStatusCodeException) {
                        feilmelding += " Response fra oppgave = ${it.responseBodyAsString}"
                    }

                    throw OppslagException(
                            feilmelding,
                            "Oppgave.oppdaterOppgave",
                            OppslagException.Level.KRITISK,
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            it)
                }
        )
    }

    fun opprettOppgave(dto: Oppgave): Long {
        val uri = UriComponentsBuilder.fromUri(oppgaveBaseUrl).path(PATH_OPPGAVE).build().toUri()
        return Result.runCatching { postForEntity<Oppgave>(uri, dto, httpHeaders()) }
                .map { it.id ?: error("Kan ikke finne oppgaveId på oppgaven $it") }
                .onFailure {
                    var feilmelding = "Feil ved oppretting av oppgave for ${dto.aktoerId}."
                    if (it is HttpStatusCodeException) {
                        feilmelding += " Response fra oppgave = ${it.responseBodyAsString}"
                    }

                    throw OppslagException(
                            feilmelding,
                            "Oppgave.opprettOppgave",
                            OppslagException.Level.MEDIUM,
                            HttpStatus.INTERNAL_SERVER_ERROR,
                            it)
                }
                .getOrThrow()
    }

    private fun lagRequestUrlMed(aktoerId: String, journalpostId: String, tema: String): URI {
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                .path(PATH_OPPGAVE)
                .queryParam("aktoerId", aktoerId)
                .queryParam("tema", tema)
                .queryParam("oppgavetype", OPPGAVE_TYPE)
                .queryParam("journalpostId", journalpostId)
                .queryParam("statuskategori", "AAPEN")
                .build()
                .toUri()
    }

    private fun requestUrl(oppgaveId: Long): URI {
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl).pathSegment(PATH_OPPGAVE, oppgaveId.toString()).build().toUri()
    }

    private fun requestOppgaveJson(requestUrl: URI): Oppgave {
        val finnOppgaveResponseDto = getForEntity<FinnOppgaveResponseDto>(requestUrl, httpHeaders())
                                     ?: error("Response fra FinnOppgave er null")
        if (finnOppgaveResponseDto.oppgaver.isEmpty()) {
            returnerteIngenOppgaver.increment()
            throw OppslagException("Ingen oppgaver funnet for $requestUrl",
                                   "oppgave",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.NOT_FOUND)
        }
        if (finnOppgaveResponseDto.oppgaver.size > 1) {
            returnerteMerEnnEnOppgave.increment()
            logger.warn("Returnerte mer enn 1 oppgave, antall: ${finnOppgaveResponseDto.oppgaver.size}, oppgave: $requestUrl")
        }
        return finnOppgaveResponseDto.oppgaver[0]
    }

    private fun httpHeaders(): HttpHeaders = HttpHeaders().apply {
        add(X_CORRELATION_ID, MDC.get(MDCConstants.MDC_CALL_ID))
    }

    companion object {

        private const val PATH_PING = "internal/alive"
        private const val PATH_OPPGAVE = "/api/v1/oppgaver"
        private const val PATH_MAPPE = "/api/v1/mapper"
        private val KONTANTSTØTTE_TEMA = Tema.KON
        private const val OPPGAVE_TYPE = "BEH_SAK"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
