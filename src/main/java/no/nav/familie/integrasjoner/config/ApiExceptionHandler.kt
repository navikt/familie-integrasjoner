package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.JournalpostNotFoundException
import no.nav.familie.integrasjoner.journalpost.JournalpostRequestException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.journalpost.selvbetjening.SafSelvbetjeningException
import no.nav.familie.integrasjoner.personopplysning.PdlGeografiskTilknytningNotFoundException
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.integrasjoner.personopplysning.PdlUnauthorizedException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import org.slf4j.LoggerFactory
import org.springframework.core.NestedExceptionUtils
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientResponseException
import org.springframework.web.context.request.async.AsyncRequestNotUsableException

@ControllerAdvice
class ApiExceptionHandler {
    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccessException(e: ResourceAccessException): ResponseEntity<Ressurs<Any>> {
        logger.warn("ResourceAccess - feil ", NestedExceptionUtils.getMostSpecificCause(e))
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure("""Det oppstod en feil. ${e.message}""", error = e))
    }

    @ExceptionHandler(RestClientResponseException::class)
    fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<Ressurs<Any>> {
        val mostSpecificCause = NestedExceptionUtils.getMostSpecificCause(e)
        secureLogger.warn(
            "RestClientResponseException : topCause=${e.javaClass.name} mostSpecificCause=${mostSpecificCause.javaClass.name} responseBody=${e.responseBodyAsString}",
            e,
        )
        logger.warn(
            "RestClientResponseException : topCause=${e.javaClass.name} mostSpecificCause=${mostSpecificCause.javaClass.name} - se securelog for detaljer",
            mostSpecificCause,
        )
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(
                failure(
                    errorMessage = "Feil mot ekstern tjeneste. ${e.statusCode.value()} ${e.responseBodyAsString} Message=${e.message}",
                    error = e,
                ),
            )
    }

    /**
     * AsyncRequestNotUsableException er en exception som blir kastet når en async request blir avbrutt. Velger
     * å skjule denne exceptionen fra loggen da den ikke er interessant for oss.
     */
    @ExceptionHandler(AsyncRequestNotUsableException::class)
    fun handlAsyncRequestNotUsableException(
        e: AsyncRequestNotUsableException,
    ): ResponseEntity<Ressurs<Any>>? {
        logger.info("En AsyncRequestNotUsableException har oppstått, som skjer når en async request blir avbrutt", e)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeaderException(e: MissingRequestHeaderException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler påkrevd request header. {}", e.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(failure("Mangler påkrevd request header", error = e))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler påkrevd request parameter. {}", e.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(failure("Mangler påkrevd request parameter", error = e))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler påkrevd request parameter. {}", e.message)
        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(failure("Mangler påkrevd request parameter", error = e))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Ressurs<Any>> {
        val mostSpecificCause = NestedExceptionUtils.getMostSpecificCause(e)
        secureLogger.error("Exception : {} - mostSpecificCause: {}", e.javaClass.name, mostSpecificCause.message, e)
        logger.warn(
            "Exception : {} - mostSpecificCause: {} - se securelog for detaljer",
            e.javaClass.name,
            mostSpecificCause.javaClass.name,
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure("""Det oppstod en feil. ${e.message}""", error = mostSpecificCause))
    }

    /**
     * Endepunktet for dokument fra SAF selvbetjening svarer med PDF. Content-Type settes eksplisitt slik at
     * feilsvaret kan skrives som JSON selv om klienten sender Accept: application/pdf.
     */
    @ExceptionHandler(SafSelvbetjeningException::class)
    fun handleSafSelvbetjeningException(e: SafSelvbetjeningException): ResponseEntity<Ressurs<Any>> {
        val mostSpecificCause = NestedExceptionUtils.getMostSpecificCause(e)
        val status = (mostSpecificCause as? RestClientResponseException)?.statusCode?.value()
        secureLogger.warn("SafSelvbetjeningException : {}", e.message, e)
        logger.warn(
            "Feil mot SAF selvbetjening. status={} mostSpecificCause={} - se securelog for detaljer",
            status,
            mostSpecificCause.javaClass.name,
        )

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .contentType(MediaType.APPLICATION_JSON)
            .body(failure(errorMessage = "Feil ved henting av dokument fra SAF selvbetjening"))
    }

    @ExceptionHandler(PdlNotFoundException::class)
    fun handlePdlNotFoundException(feil: PdlNotFoundException): ResponseEntity<Ressurs<Nothing>> {
        logger.info("Finner ikke personen i PDL")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure(frontendFeilmelding = "Finner ikke personen"))
    }

    @ExceptionHandler(PdlGeografiskTilknytningNotFoundException::class)
    fun handlePdlGeografiskTilknytningNotFoundException(feil: PdlGeografiskTilknytningNotFoundException): ResponseEntity<Ressurs<Nothing>> {
        logger.info("Finner ikke geografisk tilknytning for personen i PDL")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure(frontendFeilmelding = "Finner ikke geografisk tilknytning for person"))
    }

    @ExceptionHandler(PdlUnauthorizedException::class)
    fun handlePdlUnauthorizedException(feil: PdlUnauthorizedException): ResponseEntity<Ressurs<Nothing>> {
        incrementLoggFeil("pdl.unauthorized")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(failure(frontendFeilmelding = "Har ikke tilgang til å slå opp personen i PDL"))
    }

    @ExceptionHandler(OppslagException::class)
    fun handleOppslagException(e: OppslagException): ResponseEntity<Ressurs<Any>> {
        var feilmelding = "[${e.kilde}][${e.message}]"
        var sensitivFeilmelding = feilmelding
        if (!e.sensitiveInfo.isNullOrEmpty()) {
            sensitivFeilmelding += "[${e.sensitiveInfo}]"
        }
        if (e.error != null) {
            feilmelding += "[${e.error.javaClass.name}]"
            sensitivFeilmelding += "[${e.error.javaClass.name}]"
        }

        when (e.level) {
            OppslagException.Level.KRITISK,
            OppslagException.Level.MEDIUM,
            -> {
                secureLogger.warn("OppslagException : $sensitivFeilmelding", e.error)
                logger.warn("OppslagException : $feilmelding")

                incrementLoggFeil(e.kilde)
            }

            else -> {
                logger.info("OppslagException : $feilmelding")
            }
        }

        return ResponseEntity
            .status(e.httpStatus)
            .headers { if (e.httpStatus.value() == 401) it.set("WWW-Authenticate", "Bearer") }
            .body(failure(feilmelding, error = e))
    }

    @ExceptionHandler(JournalpostRestClientException::class)
    fun handleJournalpostRestClientException(ex: JournalpostRestClientException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost=${ex.journalpostId}"
        val errorExtMessage = byggFeilmelding(ex)
        logger.warn(errorBaseMessage, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorBaseMessage + errorExtMessage, error = ex))
    }

    @ExceptionHandler(JournalpostRequestException::class)
    fun handleJournalpostRequestException(ex: JournalpostRequestException): ResponseEntity<Ressurs<Any>> {
        val errorBaseMessage = "Feil ved henting av journalpost for ${ex.safJournalpostRequest}"
        val errorExtMessage = byggFeilmelding(ex)
        secureLogger.warn(errorBaseMessage, ex)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure(errorBaseMessage + errorExtMessage, error = ex))
    }

    @ExceptionHandler(JournalpostForbiddenException::class)
    fun handleJournalpostForbiddenException(e: JournalpostForbiddenException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Bruker eller system har ikke tilgang til saf ressurs: ${e.message}")

        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(Ressurs.ikkeTilgang(e.message ?: "Bruker eller system har ikke tilgang til saf ressurs"))
    }

    @ExceptionHandler(JournalpostNotFoundException::class)
    fun handleJournalpostNotFoundException(e: JournalpostNotFoundException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler data for journalpost med id: ${e.journalpostId}. Feil: ${e.message}")

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure(errorMessage = e.message, frontendFeilmelding = "Finner ikke journalpost"))
    }

    private fun byggFeilmelding(ex: RuntimeException): String =
        if (ex.cause is HttpStatusCodeException) {
            val cex = ex.cause as HttpStatusCodeException
            " statuscode=${cex.statusCode} body=${cex.responseBodyAsString}"
        } else {
            " klientfeilmelding=${ex.message}"
        }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
