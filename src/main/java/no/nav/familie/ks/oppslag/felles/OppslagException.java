package no.nav.familie.ks.oppslag.felles;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class OppslagException extends RuntimeException {

    private HttpStatus httpStatus;
    private String kilde;
    private Level level;
    private Throwable error;

    public enum Level {
        LAV,
        MEDIUM,
        KRITISK
    }

    public OppslagException(String message, String kilde, Level level, HttpStatus httpStatus, Exception error) {
        super(message);
        this.httpStatus = httpStatus == null ? INTERNAL_SERVER_ERROR : httpStatus;
        this.kilde = kilde;
        this.level = level;
        this.error = error;
    }

    public OppslagException(String message, String kilde, Level level, HttpStatus httpStatus) {
        this(message, kilde, level, httpStatus, null);
    }

    public OppslagException(String message, String kilde, Level level) {
        this(message, kilde, level, null, null);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getKilde() {
        return kilde;
    }

    public Level getLevel() {
        return level;
    }

    public Throwable getError() {
        return error;
    }
}
