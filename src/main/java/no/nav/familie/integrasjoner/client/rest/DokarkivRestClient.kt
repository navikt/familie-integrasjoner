package no.nav.familie.integrasjoner.client.rest

import no.nav.familie.http.client.AbstractPingableRestClient
import no.nav.familie.http.client.AbstractRestClient
import no.nav.familie.integrasjoner.dokarkiv.client.KanIkkeFerdigstilleJournalpostException
import no.nav.familie.integrasjoner.dokarkiv.client.domene.FerdigstillJournalPost
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.felles.erSystembruker
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostRequest
import no.nav.familie.kontrakter.felles.dokarkiv.OppdaterJournalpostResponse
import no.nav.familie.log.mdc.MDCConstants
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.client.RestOperations
import org.springframework.web.util.UriComponentsBuilder
import java.net.URI

@Component
class DokarkivRestClient(@Value("\${DOKARKIV_V1_URL}") private val dokarkivUrl: URI,
                         @Qualifier("jwtBearerOboOgSts") private val restOperations: RestOperations)
    : AbstractPingableRestClient(restOperations, "dokarkiv.opprett") {

    private val logger = LoggerFactory.getLogger(this::class.java)

    override val pingUri: URI = UriComponentsBuilder.fromUri(dokarkivUrl).path(PATH_PING).build().toUri()

    private val ferdigstillJournalPostClient = FerdigstillJournalPostClient(restOperations, dokarkivUrl)

    fun lagJournalpostUri(ferdigstill: Boolean): URI = UriComponentsBuilder
            .fromUri(dokarkivUrl).path(PATH_JOURNALPOST).query(QUERY_FERDIGSTILL).buildAndExpand(ferdigstill).toUri()

    fun lagJournalpost(jp: OpprettJournalpostRequest,
                       ferdigstill: Boolean): OpprettJournalpostResponse {
        val uri = lagJournalpostUri(ferdigstill)
        try {
            val postForEntity = postForEntity<OpprettJournalpostResponse>(uri, jp, headers("Z994230"))
            if (shouldLog()) {
                logger.info("Laget journalpostId=${postForEntity.journalpostId} count=${value}")
            }
            return postForEntity
        } catch (e: RuntimeException) {
            throw oppslagExceptionVed("opprettelse", e, jp.bruker?.id)
        }
    }

    fun oppdaterJournalpost(jp: OppdaterJournalpostRequest, journalpostId: String): OppdaterJournalpostResponse {
        val uri = UriComponentsBuilder.fromUri(dokarkivUrl).pathSegment(PATH_JOURNALPOST, journalpostId).build().toUri()
        try {
            val putForEntity = putForEntity<OppdaterJournalpostResponse>(uri, jp, headers("Z994708"))
            if (shouldLog()) {
                logger.info("Oppdater journalpostId=${putForEntity.journalpostId} count=${value}")
            }
            return putForEntity
        } catch (e: RuntimeException) {
            throw oppslagExceptionVed("oppdatering", e, jp.bruker?.id)
        }
    }

    fun ferdigstillJournalpost(journalpostId: String, journalførendeEnhet: String) {
        ferdigstillJournalPostClient.ferdigstillJournalpost(journalpostId, journalførendeEnhet)
    }

    private fun oppslagExceptionVed(requestType: String, e: RuntimeException, brukerId: String?): Throwable {
        val message = "Feil ved $requestType av journalpost "
        val sensitiveInfo = if (e is HttpStatusCodeException) e.responseBodyAsString else "$message for bruker $brukerId "
        return OppslagException(message,
                                "Dokarkiv",
                                OppslagException.Level.MEDIUM,
                                HttpStatus.INTERNAL_SERVER_ERROR,
                                e,
                                sensitiveInfo)
    }

    /**
     * Privat klasse for å gi egne metrics for ferdigstilling av journalpost.
     *
     */
    private class FerdigstillJournalPostClient(restOperations: RestOperations, private val dokarkivUrl: URI)
        : AbstractRestClient(restOperations, "dokarkiv.ferdigstill") {

        private val logger = LoggerFactory.getLogger(this::class.java)

        private fun ferdigstillJournalpostUri(journalpostId: String): URI {
            return UriComponentsBuilder
                    .fromUri(dokarkivUrl).path(String.format(PATH_FERDIGSTILL_JOURNALPOST, journalpostId)).build().toUri()
        }

        fun ferdigstillJournalpost(journalpostId: String, journalførendeEnhet: String) {
            val uri = ferdigstillJournalpostUri(journalpostId)
            try {
                patchForEntity<String>(uri, FerdigstillJournalPost(journalførendeEnhet), headers("Z994286"))
                if (shouldLog()) {
                    logger.info("Ferdigstiller journalpostId=${journalpostId} count=${value}")
                }
            } catch (e: RestClientResponseException) {
                if (e.rawStatusCode == HttpStatus.BAD_REQUEST.value()) {
                    throw KanIkkeFerdigstilleJournalpostException("Kan ikke ferdigstille journalpost " +
                                                                  "$journalpostId body ${e.responseBodyAsString}")
                }
                throw e
            }
        }
    }

    companion object {

        private const val PATH_PING = "isAlive"
        private const val PATH_JOURNALPOST = "rest/journalpostapi/v1/journalpost"
        private const val QUERY_FERDIGSTILL = "forsoekFerdigstill={boolean}"
        private const val PATH_FERDIGSTILL_JOURNALPOST = "rest/journalpostapi/v1/journalpost/%s/ferdigstill"
        private var value = 0;
        private fun isMottak(): Boolean {
            val consumer = MDC.get(MDCConstants.MDC_CONSUMER_ID)
            return consumer == "familie-ef-mottak" || consumer == "srvfamilie-ef-mot"
        }

        private fun isEfSak(): Boolean {
            val consumer = MDC.get(MDCConstants.MDC_CONSUMER_ID)
            return consumer == "srvfamilie-ef-sak" || consumer == "familie-ef-iverksett"
        }
        private fun shouldLog() = isMottak() || isEfSak()

        fun headers(saksbehandler: String): HttpHeaders {
            return HttpHeaders().apply {
                //if(erSystembruker())
                if (isEfSak()) {
                    add("Nav-User-Id", saksbehandler)
                }
                if (isMottak()) {
                    add("Nav-User-Id", saksbehandler)
                }
            }
        }
    }
}