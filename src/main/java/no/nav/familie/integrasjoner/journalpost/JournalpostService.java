package no.nav.familie.integrasjoner.journalpost;

import no.nav.familie.integrasjoner.journalpost.internal.Journalpost;
import no.nav.familie.integrasjoner.journalpost.internal.SafKlient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JournalpostService {


    private final SafKlient safClient;

    @Autowired
    public JournalpostService(SafKlient safClient) {
        this.safClient = safClient;
    }

    public String hentSaksnummer(String journalpostId) {
        Journalpost journalpost = safClient.hentJournalpost(journalpostId);
        if (journalpost != null && journalpost.getSak() != null && "GSAK".equals(journalpost.getSak().getArkivsaksystem())) {
            return journalpost.getSak().getArkivsaksnummer();
        }
        return null;
    }
}
