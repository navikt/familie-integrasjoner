package no.nav.familie.integrasjoner.journalpost.internal;

public class SafError {

    private String message;
    private String exceptionType;
    private String exception;

    public String getMessage() {
        return message;
    }

    public String getExceptionType() {
        return exceptionType;
    }

    public String getException() {
        return exception;
    }

    @Override
    public String toString() {
        return "SafError{" +
               "message='" + message + '\'' +
               ", exceptionType='" + exceptionType + '\'' +
               ", exception='" + exception + '\'' +
               '}';
    }
}
