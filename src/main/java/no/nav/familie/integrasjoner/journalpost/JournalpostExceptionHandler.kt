package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.journalpost.versjonertsøknad.BaksVersjonertSøknadController
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpStatusCodeException

@ControllerAdvice(basePackageClasses = [HentJournalpostController::class, BaksVersjonertSøknadController::class])
class JournalpostExceptionHandler {
    @ExceptionHandler(JournalpostRestClientException::class)
    fun handleRestClientException(ex: JournalpostRestClientException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost=${ex.journalpostId}"
        val errorExtMessage = byggFeilmelding(ex)
        LOG.warn(errorBaseMessage, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorBaseMessage + errorExtMessage, error = ex))
    }

    @ExceptionHandler(JournalpostRequestException::class)
    fun handleJournalpostForBrukerException(ex: JournalpostRequestException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost for ${ex.safJournalpostRequest}"
        val errorExtMessage = byggFeilmelding(ex)
        secureLogger.warn(errorBaseMessage, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorBaseMessage + errorExtMessage, error = ex))
    }

    @ExceptionHandler(JournalpostForbiddenException::class)
    fun handleJournalpostForbiddenException(e: JournalpostForbiddenException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Bruker eller system har ikke tilgang til saf ressurs: ${e.message}")

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Ressurs.ikkeTilgang(e.message ?: "Bruker eller system har ikke tilgang til saf ressurs"))
    }

    @ExceptionHandler(JournalpostNotFoundException::class)
    fun handleJournalpostNotFoundException(e: JournalpostNotFoundException): ResponseEntity<Ressurs<Any>> {
        LOG.warn("Mangler data for journalpost med id: ${e.journalpostId}. Feil: ${e.message}")

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure(errorMessage = e.message, frontendFeilmelding = "Finner ikke journalpost"))
    }

    private fun byggFeilmelding(ex: RuntimeException): String =
        if (ex.cause is HttpStatusCodeException) {
            val cex = ex.cause as HttpStatusCodeException
            " statuscode=${cex.statusCode} body=${cex.responseBodyAsString}"
        } else {
            " klientfeilmelding=${ex.message}"
        }

    @ExceptionHandler(RuntimeException::class)
    fun handleRequestParserException(ex: RuntimeException): ResponseEntity<Ressurs<Any>> {
        val errorMessage = "Feil ved henting av journalpost. ${ex.message}"
        LOG.warn(errorMessage, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorMessage, error = ex))
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(JournalpostExceptionHandler::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
