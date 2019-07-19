package no.nav.familie.ks.oppslag.personopplysning.internal;


import no.nav.tjeneste.virksomhet.person.v3.binding.*;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest;
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkResponse;

import javax.xml.ws.soap.SOAPFaultException;

public class PersonConsumer {

    private PersonV3 port;

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
            return port.hentPersonhistorikk(request);
        } catch (SOAPFaultException e) { // NOSONAR
            throw new RuntimeException(e);
        }
    }

}
