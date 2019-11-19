package no.nav.familie.ks.oppslag.medlemskap;

import no.nav.familie.ks.oppslag.felles.OppslagException;
import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsInfo;
import no.nav.familie.ks.oppslag.medlemskap.domain.MedlemskapsOversetter;
import no.nav.familie.ks.oppslag.medlemskap.internal.MedlClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class MedlemskapService {

    private MedlClient medlClient;
    private MedlemskapsOversetter medlemskapsOversetter;

    @Autowired
    public MedlemskapService(MedlClient medlClient, MedlemskapsOversetter medlemskapsOversetter) {
        this.medlClient = medlClient;
        this.medlemskapsOversetter = medlemskapsOversetter;
    }

    public MedlemskapsInfo hentMedlemskapsUnntak(String aktørId) {
        try {
            return medlemskapsOversetter.tilMedlemskapsInfo(medlClient.hentMedlemskapsUnntakResponse(aktørId));
        } catch (Exception e) {
            throw new OppslagException("Feil ved oppslag for Aktør " + aktørId + " og uri " + medlClient.getMedl2Uri(), "MEDL2", OppslagException.Level.MEDIUM, HttpStatus.INTERNAL_SERVER_ERROR, e);
        }
    }
}
