package no.nav.familie.ks.oppslag.journalpost.internal;

import no.nav.tjeneste.virksomhet.innsynjournal.v2.binding.*;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostRequest;
import no.nav.tjeneste.virksomhet.innsynjournal.v2.meldinger.IdentifiserJournalpostResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class InnsynJournalConsumer {
    private static final Logger LOG = LoggerFactory.getLogger(InnsynJournalConsumer.class);

    private InnsynJournalV2 port;

    public InnsynJournalConsumer(InnsynJournalV2 port) {
        this.port = port;
    }

    public IdentifiserJournalpostResponse hentJournalpost(String kanalReferanseId) {
        IdentifiserJournalpostRequest request = new IdentifiserJournalpostRequest();
        request.setKanalReferanseId(kanalReferanseId);

        try {
            return port.identifiserJournalpost(request);
        } catch (IdentifiserJournalpostJournalpostIkkeInngaaende | IdentifiserJournalpostUgyldingInput | IdentifiserJournalpostUgyldigAntallJournalposter e) {
            throw new RuntimeException("Innsyn klarte ikke å hente journalpost med kanalReferanseId=" + kanalReferanseId, e);
        } catch (IdentifiserJournalpostObjektIkkeFunnet e) {
            LOG.info("Fant ikke journalpost med kanalReferanseId={}", kanalReferanseId);
            return null;
        }
    }
}
