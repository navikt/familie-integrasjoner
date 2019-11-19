package no.nav.familie.ks.oppslag.config;

import no.nav.familie.http.azure.AzureAccessTokenException;
import no.nav.familie.ks.kontrakter.sak.Ressurs;
import no.nav.familie.ks.oppslag.felles.OppslagException;
import no.nav.security.token.support.spring.validation.interceptor.JwtTokenUnauthorizedException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.client.RestClientResponseException;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

@ControllerAdvice
public class ApiExceptionHandler {

    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    public ApiExceptionHandler() {
    }

    @ExceptionHandler({JwtTokenUnauthorizedException.class})
    public ResponseEntity<Ressurs> handleUnauthorizedException(JwtTokenUnauthorizedException e) {
        logger.warn("Kan ikke logget inn.", e);
        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(Ressurs.Companion.failure("Du er ikke logget inn.", null));
    }

    @ExceptionHandler({RestClientResponseException.class})
    public ResponseEntity<Ressurs> handleRestClientResponseException(RestClientResponseException e) {
        secureLogger.error("RestClientResponseException : {} {}", e.getResponseBodyAsString(), e);
        logger.error("RestClientResponseException : {} {} {}", e.getRawStatusCode(), e.getStatusText(), ExceptionUtils.getStackTrace(e));
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(Ressurs.Companion.failure("Feil mot ekstern tjeneste. " + e.getRawStatusCode() + " " + e.getResponseBodyAsString() + " Message=" + e.getMessage(), null));
    }

    @ExceptionHandler({MissingRequestHeaderException.class})
    public ResponseEntity<Ressurs> handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        logger.warn("Mangler påkrevd request header. {}", e.getMessage());
        return ResponseEntity
                .status(BAD_REQUEST)
                .body(Ressurs.Companion.failure("Mangler påkrevd request header", e));
    }

    @ExceptionHandler({AzureAccessTokenException.class})
    public ResponseEntity<Ressurs> handleRestClientResponseException(AzureAccessTokenException e) {
        logger.error("AzureAccessTokenException : {} ", ExceptionUtils.getStackTrace(e));
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(Ressurs.Companion.failure("Feil mot azure. Message=" + e.getMessage(), null));
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Ressurs> handleException(Exception e) {
        secureLogger.error("Exception : ", e);
        logger.error("Exception : {}", ExceptionUtils.getStackTrace(e));
        return ResponseEntity
                .status(INTERNAL_SERVER_ERROR)
                .body(Ressurs.Companion.failure("Det oppstod en feil. " + e.getMessage(), null));
    }

    @ExceptionHandler({OppslagException.class})
    public ResponseEntity<Ressurs> handAktørOppslagException(OppslagException e) {
        String feilmelding = String.format("[%s][%s]", e.getKilde(), e.getMessage());
        if (e.getError() != null) {
            feilmelding += String.format("[%s]", e.getError().getClass().getName());
        }
        switch (e.getLevel()) {
            case KRITISK:
                secureLogger.error("OppslagException : {} [{}]", feilmelding, e.getError());
                logger.error("OppslagException : {}", feilmelding);
                break;
            case MEDIUM:
                secureLogger.warn("OppslagException : {} [{}]", feilmelding, e.getError());
                logger.warn("OppslagException : {}", feilmelding);
                break;
            default:
                logger.info("OppslagException : {} {}", feilmelding);
        }

        return ResponseEntity
                .status(e.getHttpStatus())
                .body(Ressurs.Companion.failure(feilmelding, e));
    }


}
