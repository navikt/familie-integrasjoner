package no.nav.familie.integrasjoner.client.rest

import io.micrometer.core.instrument.Metrics
import no.nav.familie.integrasjoner.dokarkiv.client.domene.FerdigstillJournalPost
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostRequest
import no.nav.familie.integrasjoner.dokarkiv.client.domene.OpprettJournalpostResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.client.RestOperations
import java.net.URI
import java.util.concurrent.TimeUnit

@Component
class DokarkivClient(@Value("\${DOKARKIV_V1_URL}")
                     private val dokarkivUrl: String,
                     @Qualifier("sts") private val restOperations: RestOperations) : AbstractRestClient(restOperations) {

    val FERDIGSTILL_JOURNALPOST_DTO = FerdigstillJournalPost(9999)

    override val pingUri: URI = URI.create(String.format("%s/isAlive", dokarkivUrl))


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

    fun lagJournalpost(jp: OpprettJournalpostRequest,
                       ferdigstill: Boolean,
                       personIdent: String?): OpprettJournalpostResponse {
        val uri = URI.create(String.format("%s/rest/journalpostapi/v1/journalpost?foersoekFerdigstill=%b",
                                           dokarkivUrl,
                                           ferdigstill))
        val httpHeaders = org.springframework.http.HttpHeaders().apply {
            add(NAV_PERSONIDENTER, personIdent)
        }
        try {
            val startTime = System.nanoTime()
            val opprettJournalpostResponse: OpprettJournalpostResponse = postForEntity(uri, jp, httpHeaders)
            opprettJournalpostResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            opprettJournalpostSuccess.increment()
            return opprettJournalpostResponse

        } catch (e: Exception) {
            opprettJournalpostFailure.increment()
            throw RuntimeException("Feil ved kall mot Dokarkiv uri=$uri", e)
        } catch (e: InterruptedException) {
            opprettJournalpostFailure.increment()
            throw RuntimeException("Feil ved kall mot Dokarkiv uri=$uri", e)
        }
    }

    fun ferdigstillJournalpost(journalpostId: String) {
        val uri = URI.create(String.format("%s/rest/journalpostapi/v1/journalpost/%s/ferdigstill",
                                           dokarkivUrl,
                                           journalpostId))
        try {
            val startTime = System.nanoTime()
            patchForObject<Any>(uri, FERDIGSTILL_JOURNALPOST_DTO)
            ferdigstillJournalpostResponstid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS)
            ferdigstillJournalpostSuccess.increment()

        } catch (e: Exception) {
            ferdigstillJournalpostFailure.increment()
            throw e
        }
    }

    companion object {
        private const val NAV_PERSONIDENTER = "Nav-Personidenter"

    }


}