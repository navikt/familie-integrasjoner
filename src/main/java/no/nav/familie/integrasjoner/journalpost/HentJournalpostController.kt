package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.journalpost.domene.Journalpost
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import org.springframework.web.client.HttpStatusCodeException

@RestController @RequestMapping("/api/journalpost") @ProtectedWithClaims(issuer = "azuread")
class HentJournalpostController(private val journalpostService: JournalpostService) {

    @ExceptionHandler(JournalpostRestClientException::class)
    fun handleRestClientException(ex: JournalpostRestClientException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost=${ex.journalpostId}"
        val errorExtMessage = if (ex.cause is HttpStatusCodeException) {
            val cex = ex.cause
            " statuscode=${cex.statusCode} body=${cex.responseBodyAsString}"
        } else {
            " klientfeilmelding=${ex.message}"
        }
        LOG.warn(errorBaseMessage, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(failure(errorBaseMessage + errorExtMessage, ex))
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRequestParserException(ex: RuntimeException): ResponseEntity<Ressurs<Any>> {
        val errorMessage = "Feil ved henting av journalpost. ${ex.message}"
        LOG.warn(errorMessage, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(failure(errorMessage, ex))
    }

    @GetMapping("sak")
    fun hentSaksnummer(@RequestParam(name = "journalpostId") journalpostId: String)
            : ResponseEntity<Ressurs<Map<String, String>>> {
        val saksnummer = journalpostService.hentSaksnummer(journalpostId)
                         ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                 .body(failure("Sak mangler for journalpostId=$journalpostId", null))

        return ResponseEntity.ok(success(mapOf("saksnummer" to saksnummer), "OK"))
    }

    @GetMapping
    fun hentJournalpost(@RequestParam(name = "journalpostId") journalpostId: String)
            : ResponseEntity<Ressurs<Journalpost>> {
        return ResponseEntity.ok(success(journalpostService.hentJournalpost(journalpostId), "OK"))
    }

    @GetMapping("hentdokument/{journalpostId}/{dokumentInfoId}")
    fun hentDokument(@PathVariable journalpostId: String,
                     @PathVariable dokumentInfoId: String,
                     @RequestParam("variantFormat", required = false) variantFormat: String?)
            : ResponseEntity<Ressurs<ByteArray>> {
        return ResponseEntity.ok(success(journalpostService.hentDokument(journalpostId, dokumentInfoId, variantFormat), "OK"))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(HentJournalpostController::class.java)
    }
}