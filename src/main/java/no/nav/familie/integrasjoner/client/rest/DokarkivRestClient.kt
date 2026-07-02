package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.familie.integrasjoner.dokarkiv.client.domene.FerdigstillJournalPost
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.integrasjoner.felles.MDCOperations
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.sikkerhet.SikkerhetsContext
import no.nav.familie.kontrakter.felles.dokarkiv.AvsluttSakRequest
import no.nav.familie.kontrakter.felles.dokarkiv.GjenåpneSakRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.log.NavHttpHeaders
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.body
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokarkivRestClient(
    @Value("\${DOKARKIV_V1_URL}") private val dokarkivUrl: URI,
    @Value("\${DOKARKIV_SCOPE}") scope: String,
    entraIDRestClientFactory: EntraIDRestClientFactory,
) {
    private val restClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { SikkerhetsContext.hentJwt().tokenValue }

    fun lagJournalpostUri(ferdigstill: Boolean): URI =
        UriComponentsBuilder
            .fromUri(dokarkivUrl)
            .path(PATH_JOURNALPOST)
            .query(QUERY_FERDIGSTILL)
            .buildAndExpand(ferdigstill)
            .toUri()

    fun lagJournalpost(
        jp: OpprettJournalpostRequest,
        ferdigstill: Boolean,
        navIdent: String? = null,
    ): OpprettJournalpostResponse {
        val uri = lagJournalpostUri(ferdigstill)
        try {
            return restClient
                .post()
                .uri(uri)
                .headers { it.putAll(headers(navIdent)) }
                .body(jp)
                .retrieve()
                .body<OpprettJournalpostResponse>()!!
        } catch (feilVedJournalføring: RuntimeException) {
            if (feilVedJournalføring is HttpClientErrorException.Conflict) {
                logger.warn("409 ved oppretting av journalpost med eksternReferanseId=${jp.eksternReferanseId}. Denne journalposten er allerede journalført. Returnerer body fra feilmelding som er en OpprettJournalpostResponse.")
                return try {
                    feilVedJournalføring.getResponseBodyAs(OpprettJournalpostResponse::class.java) ?: throw feilVedJournalføring
                } catch (parsingFeil: RuntimeException) {
                    throw oppslagExceptionVed("opprettelse", feilVedJournalføring, jp.bruker?.id, "dokarkiv.opprettJournalpost")
                }
            }
            throw oppslagExceptionVed("opprettelse", feilVedJournalføring, jp.bruker?.id, "dokarkiv.opprettJournalpost")
        }
    }

    fun oppdaterJournalpost(
        jp: OppdaterJournalpostRequest,
        journalpostId: String,
        navIdent: String? = null,
    ): OppdaterJournalpostResponse {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .pathSegment(PATH_JOURNALPOST, journalpostId)
                .build()
                .toUri()
        try {
            return restClient
                .put()
                .uri(uri)
                .headers { it.putAll(headers(navIdent)) }
                .body(jp)
                .retrieve()
                .body<OppdaterJournalpostResponse>()!!
        } catch (e: RuntimeException) {
            throw oppslagExceptionVed("oppdatering", e, jp.bruker?.id, "dokarkiv.oppdaterJournalpost")
        }
    }

    fun ferdigstillJournalpost(
        journalpostId: String,
        journalførendeEnhet: String,
        navIdent: String? = null,
    ) {
        val uri = ferdigstillJournalpostUri(journalpostId)
        try {
            restClient
                .patch()
                .uri(uri)
                .headers { it.putAll(headers(navIdent)) }
                .body(FerdigstillJournalPost(journalførendeEnhet))
                .retrieve()
                .body<String>()
        } catch (e: RestClientResponseException) {
            if (e.statusCode.value() == HttpStatus.BAD_REQUEST.value()) {
                throw KanIkkeFerdigstilleJournalpostException(
                    "Kan ikke ferdigstille journalpost " +
                        "$journalpostId body ${e.responseBodyAsString}",
                )
            }
            throw OppslagException(
                "Feil ved ferdigstilling av journalpost",
                "dokarkiv.ferdigstill.feil",
                OppslagException.Level.MEDIUM,
                HttpStatus.INTERNAL_SERVER_ERROR,
                e,
            )
        }
    }

    fun avsluttSak(
        request: AvsluttSakRequest,
    ) {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_AVSLUTT_SAK)
                .build()
                .toUri()
        try {
            restClient
                .patch()
                .uri(uri)
                .body(request)
                .retrieve()
                .body<String>()
        } catch (e: RuntimeException) {
            throw oppslagExceptionVed("avslutting av sak i dokarkiv", e, request.bruker.id, "dokarkiv.avsluttSak")
        }
    }

    fun gjenåpneSak(
        gjenåpneSakRequest: GjenåpneSakRequest,
    ) {
        val uri =
            UriComponentsBuilder
                .fromUri(dokarkivUrl)
                .path(PATH_GJENÅPNE_SAK)
                .build()
                .toUri()
        try {
            restClient
                .patch()
                .uri(uri)
                .body(gjenåpneSakRequest)
                .retrieve()
                .body<String>()
        } catch (e: RuntimeException) {
            throw oppslagExceptionVed("gjenåpning av sak i dokarkiv", e, gjenåpneSakRequest.bruker.id, "dokarkiv.gjenaapneSak")
        }
    }

    private fun oppslagExceptionVed(
        requestType: String,
        e: RuntimeException,
        brukerId: String?,
        kilde: String,
    ): Throwable {
        val message = "Feil ved $requestType av journalpost "
        val sensitiveInfo = if (e is HttpStatusCodeException) e.responseBodyAsString else "$message for bruker $brukerId "
        val httpStatus = if (e is HttpStatusCodeException) e.statusCode else HttpStatus.INTERNAL_SERVER_ERROR
        return OppslagException(
            message,
            kilde,
            OppslagException.Level.MEDIUM,
            httpStatus,
            e,
            sensitiveInfo,
        )
    }

    private fun ferdigstillJournalpostUri(journalpostId: String): URI =
        UriComponentsBuilder
            .fromUri(dokarkivUrl)
            .path(String.format(PATH_FERDIGSTILL_JOURNALPOST, journalpostId))
            .build()
            .toUri()

    companion object {
        private val logger = LoggerFactory.getLogger(DokarkivRestClient::class.java)

        private const val PATH_JOURNALPOST = "rest/journalpostapi/v1/journalpost"
        private const val QUERY_FERDIGSTILL = "forsoekFerdigstill={boolean}"
        private const val PATH_FERDIGSTILL_JOURNALPOST = "rest/journalpostapi/v1/journalpost/%s/ferdigstill"
        private const val PATH_AVSLUTT_SAK = "rest/journalpostapi/v1/sak/avsluttSak"
        private const val PATH_GJENÅPNE_SAK = "rest/journalpostapi/v1/sak/gjenaapneSak"
        private const val NAV_CALL_ID = "Nav-Callid"

        private val NAVIDENT_REGEX = """^[a-zA-Z]\d{6}$""".toRegex()

        fun headers(navIdent: String?): HttpHeaders =
            HttpHeaders().apply {
                add(NAV_CALL_ID, MDCOperations.getCallId())
                if (!navIdent.isNullOrEmpty()) {
                    if (NAVIDENT_REGEX.matches(navIdent)) {
                        add(NavHttpHeaders.NAV_USER_ID.asString(), navIdent)
                    } else {
                        logger.warn("Sender ikke med navIdent navIdent=$navIdent")
                    }
                }
            }
    }
}
