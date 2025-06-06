package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.journalpost.JournalpostForbiddenException
import no.nav.familie.integrasjoner.journalpost.JournalpostNotFoundException
import no.nav.familie.integrasjoner.journalpost.JournalpostRequestException
import no.nav.familie.integrasjoner.journalpost.JournalpostRestClientException
import no.nav.familie.integrasjoner.personopplysning.PdlNotFoundException
import no.nav.familie.integrasjoner.personopplysning.PdlUnauthorizedException
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.failure
import no.nav.security.token.support.client.core.OAuth2ClientException
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException
import org.apache.commons.lang3.exception.ExceptionUtils
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
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

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(e: JwtTokenUnauthorizedException?): ResponseEntity<Ressurs<Any>> {
        logger.warn("Kan ikke logget inn.", e)
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(failure(errorMessage = "Du er ikke logget inn.", frontendFeilmelding = "Du er ikke logget inn", error = e))
    }

    @ExceptionHandler(ResourceAccessException::class)
    fun handleResourceAccessException(e: ResourceAccessException): ResponseEntity<Ressurs<Any>> {
        logger.warn("ResourceAccess - feil ", e)
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure("""Det oppstod en feil. ${e.message}""", error = e))
    }

    @ExceptionHandler(RestClientResponseException::class)
    fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<Ressurs<Any>> {
        secureLogger.warn("RestClientResponseException : ${e.responseBodyAsString}", e)
        logger.warn(
            "RestClientResponseException : {} {} {}",
            e.statusCode.value(),
            e.statusText,
            ExceptionUtils.getStackTrace(e),
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

    @ExceptionHandler(OAuth2ClientException::class)
    fun handleOAuth2ClientException(e: OAuth2ClientException): ResponseEntity<Ressurs<Any>> {
        logger.error("OAuth2ClientException : {} ", ExceptionUtils.getStackTrace(e))
        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure("Feil mot azure. Message=${e.message}", error = e))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Ressurs<Any>> {
        secureLogger.error("Exception : ", e)
        logger.warn("Exception : {} - se securelog for detaljer", e.javaClass.name)

        return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(failure("""Det oppstod en feil. ${e.message}""", error = e))
    }

    @ExceptionHandler(PdlNotFoundException::class)
    fun handlePdlNotFoundException(feil: PdlNotFoundException): ResponseEntity<Ressurs<Nothing>> {
        logger.info("Finner ikke personen i PDL")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(failure(frontendFeilmelding = "Finner ikke personen"))
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

            else -> logger.info("OppslagException : $feilmelding")
        }

        return ResponseEntity
            .status(e.httpStatus)
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
