package no.nav.familie.integrasjoner.config

import no.nav.familie.integrasjoner.felles.OppslagException
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
import org.springframework.web.client.RestClientResponseException

@ControllerAdvice
class ApiExceptionHandler {

    private val logger = LoggerFactory.getLogger(ApiExceptionHandler::class.java)

    @ExceptionHandler(JwtTokenUnauthorizedException::class)
    fun handleUnauthorizedException(e: JwtTokenUnauthorizedException?): ResponseEntity<Ressurs<Any>> {
        logger.warn("Kan ikke logget inn.", e)
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(failure("Du er ikke logget inn.", e))
    }

    @ExceptionHandler(RestClientResponseException::class)
    fun handleRestClientResponseException(e: RestClientResponseException): ResponseEntity<Ressurs<Any>> {
        secureLogger.error("RestClientResponseException : {} {}", e.responseBodyAsString, e)
        logger.error("RestClientResponseException : {} {} {}",
                     e.rawStatusCode,
                     e.statusText,
                     ExceptionUtils.getStackTrace(e))
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(failure("Feil mot ekstern tjeneste. ${e.rawStatusCode} ${e.responseBodyAsString} Message=${e.message}",
                              e))
    }

    @ExceptionHandler(MissingRequestHeaderException::class)
    fun handleMissingRequestHeaderException(e: MissingRequestHeaderException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler påkrevd request header. {}", e.message)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(failure("Mangler påkrevd request header", e))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(e: MissingServletRequestParameterException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler påkrevd request parameter. {}", e.message)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(failure("Mangler påkrevd request parameter", e))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(e: HttpMessageNotReadableException): ResponseEntity<Ressurs<Any>> {
        logger.warn("Mangler påkrevd request parameter. {}", e.message)
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(failure("Mangler påkrevd request parameter", e))
    }

    @ExceptionHandler(OAuth2ClientException::class)
    fun handleRestClientResponseException(e: OAuth2ClientException): ResponseEntity<Ressurs<Any>> {
        logger.error("OAuth2ClientException : {} ", ExceptionUtils.getStackTrace(e))
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(failure("Feil mot azure. Message=${e.message}", e))
    }

    @ExceptionHandler(Exception::class)
    fun handleException(e: Exception): ResponseEntity<Ressurs<Any>> {
        secureLogger.error("Exception : ", e)
        logger.error("Exception : {} {}", e.javaClass.name, e.message, e)
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(failure("""Det oppstod en feil. ${e.message}""", e))
    }

    @ExceptionHandler(OppslagException::class)
    fun handAktørOppslagException(e: OppslagException): ResponseEntity<Ressurs<Any>> {
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
            OppslagException.Level.KRITISK -> {
                secureLogger.error("OppslagException : {} [{}]", sensitivFeilmelding, e.error)
                logger.error("OppslagException : {}", feilmelding)
            }

            OppslagException.Level.MEDIUM -> {
                secureLogger.warn("OppslagException : {} [{}]", sensitivFeilmelding, e.error)
                logger.warn("OppslagException : {}", feilmelding)
            }

            else -> logger.info("OppslagException : {} {}", feilmelding)
        }
        return ResponseEntity
                .status(e.httpStatus)
                .body(failure(feilmelding, e))
    }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}