package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.util.UriUtil
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.oppgave.DeprecatedFinnOppgaveRequest
import no.nav.familie.integrasjoner.oppgave.DeprecatedFinnOppgaveResponseDto
import no.nav.familie.integrasjoner.oppgave.DeprecatedOppgave
import no.nav.familie.integrasjoner.oppgave.domene.OppgaveRequest
import no.nav.familie.integrasjoner.oppgave.domene.limitMotOppgave
import no.nav.familie.integrasjoner.oppgave.domene.toDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.Tema
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
import java.time.LocalDate
import kotlin.math.min

@Component
class OppgaveRestClient(@Value("\${OPPGAVE_URL}") private val oppgaveBaseUrl: URI,
                        @Qualifier("sts") private val restTemplate: RestOperations)
    : AbstractPingableRestClient(restTemplate, "oppgave") {

    override val pingUri = UriUtil.uri(oppgaveBaseUrl, PATH_PING)

    private val returnerteIngenOppgaver = Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "ingen")
    private val returnerteMerEnnEnOppgave = Metrics.counter("oppslag.oppgave.response", "antall.oppgaver", "flerEnnEn")

    private val logger = LoggerFactory.getLogger(OppgaveRestClient::class.java)

    fun finnOppgave(request: Oppgave): Oppgave {
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

    @Deprecated("Bruk finnOppgaver med FinnOppgaveRequest")
    fun finnOppgaver(tema: String,
                     behandlingstema: String?,
                     oppgavetype: String?,
                     tildeltEnhetsnr: String?,
                     tilordnetRessurs: String?,
                     journalpostId: String?): List<DeprecatedOppgave> {

        tailrec fun finnAlleOppgaver(oppgaver: List<DeprecatedOppgave> = listOf()): List<DeprecatedOppgave> {
            val limit = 50

            val uriBuilder = UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                    .path(PATH_OPPGAVE)
                    .queryParam("statuskategori", "AAPEN")
                    .queryParam("aktivDatoTom", LocalDate.now().toString())
                    .queryParam("tema", tema)

            behandlingstema?.apply { uriBuilder.queryParam("behandlingstema", this) }
            oppgavetype?.apply { uriBuilder.queryParam("oppgavetype", this) }
            tildeltEnhetsnr?.apply { uriBuilder.queryParam("tildeltEnhetsnr", this) }
            tilordnetRessurs?.apply { uriBuilder.queryParam("tilordnetRessurs", this) }
            journalpostId?.apply { uriBuilder.queryParam("journalpostId", this) }

            val uri = uriBuilder
                    .queryParam("limit", limit.toString())
                    .queryParam("offset", oppgaver.size.toString())
                    .build()
                    .toUri()

            val finnOppgaveResponseDto = getForEntity<DeprecatedFinnOppgaveResponseDto>(uri, httpHeaders())
            val nyeOppgaver = oppgaver + finnOppgaveResponseDto.oppgaver

            return when (nyeOppgaver.size < finnOppgaveResponseDto.antallTreffTotalt) {
                true -> finnAlleOppgaver(nyeOppgaver)
                false -> nyeOppgaver
            }
        }

        return finnAlleOppgaver()
    }

    fun finnOppgaverV2(deprecatedFinnOppgaveRequest: DeprecatedFinnOppgaveRequest): DeprecatedFinnOppgaveResponseDto {

        val limitMotOppgave = 50

        fun uriMotOppgave(offset: Long): URI {
            return deprecatedFinnOppgaveRequest.run {
                val uriBuilder = UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                        .path(PATH_OPPGAVE)
                        .queryParam("statuskategori", "AAPEN")
                        .queryParam("tema", tema)
                        .queryParam("sorteringsfelt", "OPPRETTET_TIDSPUNKT")
                        .queryParam("sorteringsrekkefolge", "DESC")
                        .queryParam("limit", limitMotOppgave)
                        .queryParam("offset", offset)

                behandlingstema?.apply { uriBuilder.queryParam("behandlingstema", this) }
                oppgavetype?.apply { uriBuilder.queryParam("oppgavetype", this) }
                enhet?.apply { uriBuilder.queryParam("tildeltEnhetsnr", this) }
                tildeltRessurs?.apply { uriBuilder.queryParam("tildeltRessurs", if (this) "true" else "false") }
                if (tilordnetRessurs != null)
                    tilordnetRessurs.apply { uriBuilder.queryParam("tilordnetRessurs", this) }
                else
                    saksbehandler?.apply { uriBuilder.queryParam("tilordnetRessurs", this) }
                journalpostId?.apply { uriBuilder.queryParam("journalpostId", this) }
                opprettetFomTidspunkt?.apply { uriBuilder.queryParam("opprettetFom", this) }
                opprettetTomTidspunkt?.apply { uriBuilder.queryParam("opprettetTom", this) }
                fristFomDato?.apply { uriBuilder.queryParam("fristFom", this) }
                fristTomDato?.apply { uriBuilder.queryParam("fristTom", this) }
                aktivFomDato?.apply { uriBuilder.queryParam("aktivDatoFom", this) }
                aktivTomDato?.apply { uriBuilder.queryParam("aktivDatoTom", this) }

                uriBuilder.build().toUri()
            }
        }

        var offset = deprecatedFinnOppgaveRequest.offset ?: 0
        val oppgaverOgAntall = getForEntity<DeprecatedFinnOppgaveResponseDto>(uriMotOppgave(offset), httpHeaders())
        val oppgaver: MutableList<DeprecatedOppgave> = oppgaverOgAntall.oppgaver.toMutableList()
        val grense =
                if (deprecatedFinnOppgaveRequest.limit == null) oppgaverOgAntall.antallTreffTotalt
                else offset + deprecatedFinnOppgaveRequest.limit
        offset += limitMotOppgave

        while (offset < grense) {
            val nyeOppgaver = getForEntity<DeprecatedFinnOppgaveResponseDto>(uriMotOppgave(offset), httpHeaders())
            oppgaver.addAll(nyeOppgaver.oppgaver)
            offset += limitMotOppgave
        }

        return DeprecatedFinnOppgaveResponseDto(oppgaverOgAntall.antallTreffTotalt, oppgaver)
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
            val nyeOppgaver =
                    getForEntity<DeprecatedFinnOppgaveResponseDto>(buildOppgaveRequestUri(oppgaveRequest
                                                                                                  .copy(offset = offset,
                                                                                                        limit = min((grense - offset),
                                                                                                                    limitMotOppgave))),
                                                                   httpHeaders())
            oppgaver.addAll(nyeOppgaver.oppgaver)
            offset += limitMotOppgave
        }
        return DeprecatedFinnOppgaveResponseDto(oppgaverOgAntall.antallTreffTotalt, oppgaver)
    }

    fun buildOppgaveRequestUri(oppgaveRequest: OppgaveRequest) =
            UriComponentsBuilder.fromUri(oppgaveBaseUrl)
                    .path(PATH_OPPGAVE)
                    .queryParams(oppgaveRequest.toQueryParams())
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
                .map { it?.id ?: error("Kan ikke finne oppgaveId på oppgaven $it") }
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
                .build()
                .toUri()
    }

    private fun requestUrl(oppgaveId: Long): URI {
        return UriComponentsBuilder.fromUri(oppgaveBaseUrl).pathSegment(PATH_OPPGAVE, oppgaveId.toString()).build().toUri()
    }

    private fun requestOppgaveJson(requestUrl: URI): Oppgave {
        val finnOppgaveResponseDto = getForEntity<FinnOppgaveResponseDto>(requestUrl, httpHeaders())
        if (finnOppgaveResponseDto == null) error("Response fra FinnOppgave er null")
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
        private val KONTANTSTØTTE_TEMA = Tema.KON
        private const val OPPGAVE_TYPE = "BEH_SAK"
        private const val X_CORRELATION_ID = "X-Correlation-ID"
    }
}
