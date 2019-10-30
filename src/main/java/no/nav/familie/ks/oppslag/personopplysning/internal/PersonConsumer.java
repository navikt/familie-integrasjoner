package no.nav.familie.ks.oppslag.personopplysning.internal;


import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;

import javax.xml.ws.soap.SOAPFaultException;
import java.util.concurrent.TimeUnit;

public class PersonConsumer {

    private PersonV3 port;
    private final Timer personResponsTid = Metrics.timer("personV3.respons.tid");
    private final Counter personSuccess = Metrics.counter("personV3.response", "status", "success");
    private final Counter personFailure = Metrics.counter("personV3.response", "status", "failure");

    public PersonConsumer(PersonV3 port) {
        this.port = port;
    }

    public HentPersonResponse hentPersonResponse(HentPersonRequest request) throws HentPersonPersonIkkeFunnet, HentPersonSikkerhetsbegrensning {
        try {
            return port.hentPerson(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw new RuntimeException(e);
        }
    }

    /**
     * Henter personhistorikk i henhold til request
     *
     * @param request request
     * @return respons
     * @throws HentPersonhistorikkSikkerhetsbegrensning når bruker ikke har tilgang
     * @throws HentPersonhistorikkPersonIkkeFunnet      når bruker ikke finnes
     */
    public HentPersonhistorikkResponse hentPersonhistorikkResponse(HentPersonhistorikkRequest request) throws HentPersonhistorikkSikkerhetsbegrensning, HentPersonhistorikkPersonIkkeFunnet {
        try {
            long startTime = System.nanoTime();
            HentPersonhistorikkResponse response = port.hentPersonhistorikk(request);
            personResponsTid.record(System.nanoTime() - startTime, TimeUnit.NANOSECONDS);
            personSuccess.increment();
            return response;
        } catch (SOAPFaultException e) { // NOSONAR
            personFailure.increment();
            throw new RuntimeException(e);
        }
    }

    public void ping() {
        port.ping();
    }

}
