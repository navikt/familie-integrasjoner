package no.nav.familie.ks.oppslag.felles;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.net.URI;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class OppslagException extends RuntimeException {

    private static final Logger SECURELOG = LoggerFactory.getLogger("secureLogger");
    private static final Logger LOG = LoggerFactory.getLogger(OppslagException.class);

    public OppslagException(String msg, Exception e, URI uri) {
        super(msg);

        SECURELOG.info("Ukjent feil ved oppslag mot {}. {}", uri, e.getMessage());
        LOG.warn("Ukjent feil ved oppslag mot '" + uri + "'.");
    }
}
