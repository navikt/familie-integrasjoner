package no.nav.familie.ks.oppslag.journalpost;

import no.nav.familie.ks.oppslag.journalpost.internal.Journalpost;
import no.nav.familie.ks.oppslag.journalpost.internal.SafKlient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JournalpostService {

    @Autowired
    private SafKlient safClient;


    public String hentSaksnummer(String journalpostId) {
        Journalpost journalpost =  safClient.hentJournalpost(journalpostId);
        if (journalpost != null && journalpost.getSak() != null && "GSAK".equals(journalpost.getSak().getArkivsaksystem())) {
            return journalpost.getSak().getArkivsaksnummer();
        }
        return null;
    }
}
