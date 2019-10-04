package no.nav.familie.ks.oppslag.oppgave.internal;

import no.nav.familie.ks.kontrakter.oppgave.Oppgave;
import no.nav.familie.ks.oppslag.felles.ws.DateUtil;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.BehandleOppgaveV1;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSOppgaveIkkeFunnetException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSOptimistiskLasingException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.WSSikkerhetsbegrensningException;
import no.nav.tjeneste.virksomhet.behandleoppgave.v1.meldinger.*;
import org.springframework.remoting.soap.SoapFaultException;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class OppgaveConsumer {

    private static final String FAGOMRÅDE_KODE = "KON";
    private final String OPPGAVETYPE_KODE = "BEH_SAK_KON";
    private final String PRIORITET_KODE = "NORM_KON";
    private static final int DEFAULT_OPPGAVEFRIST_DAGER = 2;

    private BehandleOppgaveV1 port;

    public OppgaveConsumer(BehandleOppgaveV1 port) {
        this.port = port;
    }

    public WSOpprettOppgaveResponse opprettOppgave(Oppgave request) throws WSSikkerhetsbegrensningException, SoapFaultException {
        return port.opprettOppgave(tilWSOpprett(request));
    }

    public Boolean oppdaterOppgave(Oppgave request) throws
            WSSikkerhetsbegrensningException, WSOppgaveIkkeFunnetException, WSOptimistiskLasingException, SoapFaultException {
        port.lagreOppgave(tilWSLagre(request));
        return true;
    }

    private WSOpprettOppgaveRequest tilWSOpprett(Oppgave request) {
        WSOppgave oppgave = new WSOppgave()
                .withSaksnummer(request.getGosysSakId())
                .withAnsvarligEnhetId(request.getBehandlendeEnhetId())
                .withFagomradeKode(FAGOMRÅDE_KODE)
                .withGjelderBruker(new WSAktor().withIdent(request.getFnr()))
                .withAktivFra(DateUtil.convertToXMLGregorianCalendar(iDag().atStartOfDay()))
                .withAktivTil(DateUtil.convertToXMLGregorianCalendar(helgeJustertFrist(iDag().plusDays(avklarFrist(request)))))
                .withOppgavetypeKode(OPPGAVETYPE_KODE)
                .withPrioritetKode(PRIORITET_KODE)
                .withBeskrivelse(request.getBeskrivelse())
                .withLest(false);

        return new WSOpprettOppgaveRequest()
                .withOpprettetAvEnhetId(Integer.parseInt(request.getBehandlendeEnhetId()))
                .withWsOppgave(oppgave);
    }

    private WSLagreOppgaveRequest tilWSLagre(Oppgave request) {
        WSLagreOppgave oppgave = new WSLagreOppgave()
                .withOppgaveId(Integer.parseInt(request.getEksisterendeOppgaveId()))
                .withSaksnummer(request.getGosysSakId())
                .withBeskrivelse(request.getBeskrivelse());
        return new WSLagreOppgaveRequest()
                .withEndretAvEnhetId(Integer.parseInt(request.getBehandlendeEnhetId()))
                .withWsLagreOppgave(oppgave);
    }

    private LocalDate iDag() {
        return LocalDate.now(ZoneId.systemDefault());
    }

    private int avklarFrist(Oppgave request) {
        return request.getBehandlingsfristDager() > 0 ? request.getBehandlingsfristDager() : DEFAULT_OPPGAVEFRIST_DAGER;
    }

    // Sett frist til mandag hvis fristen er i helgen.
    private LocalDateTime helgeJustertFrist(LocalDate dato) {
        if (dato.getDayOfWeek().getValue() > DayOfWeek.FRIDAY.getValue()) {
            return dato.plusDays(1L + DayOfWeek.SUNDAY.getValue() - dato.getDayOfWeek().getValue()).atStartOfDay();
        }
        return dato.atStartOfDay();
    }
}
