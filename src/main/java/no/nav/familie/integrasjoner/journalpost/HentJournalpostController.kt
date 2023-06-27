package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.HttpStatusCodeException

@RestController
@RequestMapping("/api/journalpost")
@ProtectedWithClaims(issuer = "azuread")
class HentJournalpostController(private val journalpostService: JournalpostService) {

    @ExceptionHandler(JournalpostRestClientException::class)
    fun handleRestClientException(ex: JournalpostRestClientException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost=${ex.journalpostId}"
        val errorExtMessage = byggFeilmelding(ex)
        LOG.warn(errorBaseMessage, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorBaseMessage + errorExtMessage, error = ex))
    }

    @ExceptionHandler(JournalpostRequestException::class)
    fun handleJournalpostForBrukerException(ex: JournalpostRequestException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost for ${ex.safJournalpostRequest}"
        val errorExtMessage = byggFeilmelding(ex)
        secureLogger.warn(errorBaseMessage, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorBaseMessage + errorExtMessage, error = ex))
    }

    @ExceptionHandler(JournalpostForbiddenException::class)
    fun handleJournalpostForbiddenException(e: JournalpostForbiddenException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Bruker eller system ikke tilgang til saf ressurs: ${e.message}")

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Ressurs.ikkeTilgang(e.message ?: "Bruker eller system har ikke tilgang til saf ressurs"))
    }

    private fun byggFeilmelding(ex: RuntimeException): String {
        return if (ex.cause is HttpStatusCodeException) {
            val cex = ex.cause as HttpStatusCodeException
            " statuscode=${cex.statusCode} body=${cex.responseBodyAsString}"
        } else {
            " klientfeilmelding=${ex.message}"
        }
    }

    @ExceptionHandler(RuntimeException::class)
    fun handleRequestParserException(ex: RuntimeException): ResponseEntity<Ressurs<Any>> {
        val errorMessage = "Feil ved henting av journalpost. ${ex.message}"
        LOG.warn(errorMessage, ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorMessage, error = ex))
    }

    @GetMapping("sak")
    fun hentSaksnummer(@RequestParam(name = "journalpostId") journalpostId: String): ResponseEntity<Ressurs<Map<String, String>>> {
        val saksnummer = journalpostService.hentSaksnummer(journalpostId)
            ?: return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(failure("Sak mangler for journalpostId=$journalpostId", null))

        return ResponseEntity.ok(success(mapOf("saksnummer" to saksnummer), "OK"))
    }

    @GetMapping
    fun hentJournalpost(@RequestParam(name = "journalpostId") journalpostId: String): ResponseEntity<Ressurs<Journalpost>> {
        return ResponseEntity.ok(success(journalpostService.hentJournalpost(journalpostId), "OK"))
    }

    @PostMapping
    fun hentJournalpostForBruker(@RequestBody journalposterForBrukerRequest: JournalposterForBrukerRequest): ResponseEntity<Ressurs<List<Journalpost>>> {
        return ResponseEntity.ok(success(journalpostService.finnJournalposter(journalposterForBrukerRequest), "OK"))
    }

    @PostMapping("temaer")
    fun hentJournalpostForBrukerOgTema(@RequestBody journalposterForVedleggRequest: JournalposterForVedleggRequest): ResponseEntity<Ressurs<List<Journalpost>>> {
        return ResponseEntity.ok(success(journalpostService.finnJournalposter(journalposterForVedleggRequest), "OK"))
    }

    @GetMapping("hentdokument/{journalpostId}/{dokumentInfoId}")
    fun hentDokument(
        @PathVariable journalpostId: String,
        @PathVariable dokumentInfoId: String,
        @RequestParam("variantFormat", required = false) variantFormat: String?,
    ): ResponseEntity<Ressurs<ByteArray>> {
        return ResponseEntity.ok(
            success(
                journalpostService.hentDokument(journalpostId, dokumentInfoId, variantFormat ?: "ARKIV"),
                "OK",
            ),
        )
    }

    companion object {

        private val LOG = LoggerFactory.getLogger(HentJournalpostController::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
