package no.nav.familie.ks.oppslag.journalpost;

import no.nav.familie.ks.oppslag.journalpost.internal.InnsynJournalConsumer;
import no.nav.familie.ks.oppslag.journalpost.internal.Journalpost;
import no.nav.familie.ks.oppslag.journalpost.internal.SafKlient;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JournalpostService {


    private final SafKlient safClient;
    private final InnsynJournalConsumer innsynJournalConsumer;

    @Autowired
    public JournalpostService(SafKlient safClient, InnsynJournalConsumer innsynJournalConsumer) {
        this.safClient = safClient;
        this.innsynJournalConsumer = innsynJournalConsumer;
    }

    public String hentSaksnummer(String journalpostId) {
        Journalpost journalpost = safClient.hentJournalpost(journalpostId);
        if (journalpost != null && journalpost.getSak() != null && "GSAK".equals(journalpost.getSak().getArkivsaksystem())) {
            return journalpost.getSak().getArkivsaksnummer();
        }
        return null;
    }

    public String hentJournalpostId(String kanalReferanseId) {
        IdentifiserJournalpostResponse response = innsynJournalConsumer.hentJournalpost(kanalReferanseId);
        if (response != null) {
            return response.getJournalpostId();
        }
        return null;
    }
}
