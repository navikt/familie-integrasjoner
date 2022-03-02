package no.nav.familie.integrasjoner.felles;

import org.springframework.http.HttpStatus;

import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class OppslagException extends RuntimeException {

    private HttpStatus httpStatus;
    private String kilde;
    private String sensitiveInfo;
    private Level level;
    private Throwable error;

    public enum Level {
        LAV,
        MEDIUM,
        KRITISK
    }

    public OppslagException(String message, String kilde, Level level, HttpStatus httpStatus, Throwable error, String sensitiveInfo) {
        super(message, error);
        this.httpStatus = httpStatus == null ? INTERNAL_SERVER_ERROR : httpStatus;
        this.kilde = kilde;
        this.sensitiveInfo = sensitiveInfo;
        this.level = level;
        this.error = error;
    }

    public OppslagException(String message, String kilde, Level level, HttpStatus httpStatus, Throwable error) {
        this(message, kilde, level, httpStatus, error, null);
    }

    public OppslagException(String message, String kilde, Level level, HttpStatus httpStatus) {
        this(message, kilde, level, httpStatus, null, null);
    }

    public OppslagException(String message, String kilde, Level level) {
        this(message, kilde, level, null, null, null);
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getKilde() {
        return kilde;
    }

    public String getSensitiveInfo() {
        return sensitiveInfo;
    }

    public Level getLevel() {
        return level;
    }

    public Throwable getError() {
        return error;
    }
}
