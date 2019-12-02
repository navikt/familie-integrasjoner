package no.nav.familie.integrasjoner.oppgave;

import no.nav.familie.integrasjoner.oppgave.internal.OppgaveClient;
import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.oppgave.v1.OppgaveJsonDto;
import no.nav.sbl.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.ApplicationScope;

@Service
@ApplicationScope
public class OppgaveService {

    private static final Logger LOG = LoggerFactory.getLogger(OppgaveService.class);
    private final OppgaveClient oppgaveClient;

    @Autowired
    public OppgaveService(OppgaveClient oppgaveClient) {
        this.oppgaveClient = oppgaveClient;
    }

    Long oppdaterOppgave(Oppgave request) {
        OppgaveJsonDto oppgaveJsonDto;
        if (StringUtils.nullOrEmpty(request.getEksisterendeOppgaveId())) {
            oppgaveJsonDto = oppgaveClient.finnOppgave(request);
        } else {
            oppgaveJsonDto = oppgaveClient.finnOppgave(request.getEksisterendeOppgaveId());
        }
        oppgaveClient.oppdaterOppgave(oppgaveJsonDto, request.getBeskrivelse());
        return oppgaveJsonDto.getId();
    }
}
