package no.nav.familie.ks.oppslag.oppgave;


import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.oppslag.oppgave.internal.OppgaveConsumer;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSOppgaveIkkeFunnetException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSOptimistiskLasingException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.remoting.soap.SoapFaultException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

import static org.springframework.http.HttpStatus.*;

@Service
@ApplicationScope
public class OppgaveService {

    private static final Logger LOG = LoggerFactory.getLogger(OppgaveService.class);
    private final OppgaveConsumer oppgaveConsumer;

    @Autowired
    public OppgaveService(OppgaveConsumer oppgaveConsumer) {
        this.oppgaveConsumer = oppgaveConsumer;
    }

    ResponseEntity opprettEllerOppdaterOppgave(Oppgave request) {
        try {
            if (request.getEksisterendeOppgaveId() != null) {
                return ResponseEntity.ok(oppgaveConsumer.oppdaterOppgave(request));
            } else {
                return opprettOppgaveResponse(request);
            }
        } catch (WSSikkerhetsbegrensningException | WSOppgaveIkkeFunnetException | WSOptimistiskLasingException e) {
            return ResponseEntity.status(setPassendeStatus(e)).header("message", e.getMessage()).build();
        } catch (SoapFaultException e) {
            return ResponseEntity.status(BAD_GATEWAY)
                    .header("message", String.format("SOAP tjenesten returnerte en SOAP Fault: %s", e.getMessage())).build();
        }
    }

    private ResponseEntity opprettOppgaveResponse(Oppgave request) throws WSSikkerhetsbegrensningException {
        var response = oppgaveConsumer.opprettOppgave(request);
        if (response.getOppgaveId() != null) {
            return ResponseEntity.ok(response.getOppgaveId());
        }
        return ResponseEntity.status(EXPECTATION_FAILED)
                .header("message", "Ugyldig respons: Fikk ingen oppgaveId tilbake fra GSAK").build();
    }

    private HttpStatus setPassendeStatus(Exception e) {
        if (e instanceof WSSikkerhetsbegrensningException) {
            LOG.info("Ikke tilgang til å opprette eller oppdatere oppgave i Gosys.");
            return UNAUTHORIZED;
        } else if (e instanceof WSOppgaveIkkeFunnetException) {
            LOG.info("Prøver å oppdatere oppgave som ikke finnes i Gosys.");
            return NOT_FOUND;
        } else if (e instanceof WSOptimistiskLasingException) {
            LOG.info("WSLagreOppgaveRequest returnerte WSOptimistiskLasingException...");
            return BAD_REQUEST;
        } else {
            LOG.info("Det oppstod en uventet feil.");
            return EXPECTATION_FAILED;
        }
    }
}
