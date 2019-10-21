package no.nav.familie.ks.oppslag.tilgangskontroll;

import no.nav.familie.ks.oppslag.egenansatt.EgenAnsattService;
import no.nav.familie.ks.oppslag.personopplysning.domene.Personinfo;
import no.nav.familie.ks.oppslag.tilgangskontroll.domene.Tilgang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import static no.nav.familie.ks.oppslag.tilgangskontroll.domene.AdRoller.*;

@Service
public class TilgangsKontrollService {

    public static final String GEOGRAFISK = "GEOGRAFISK";
    public static final String TILGANGTILBRUKER = "tilgangtilbruker";
    public static final String TILGANGTILTJENESTEN = "tilgangtiltjenesten";
    public static final String TILGANGTILENHET = "tilgangtilenhet";

//    private final LdapService ldapService;
    private final EgenAnsattService egenAnsattService;
//    private final GeografiskTilgangService geografiskTilgangService;
//    private final OrganisasjonRessursEnhetService organisasjonRessursEnhetService;

    private final static String ENHET = "ENHET";
    public final static String DISKRESJONSKODE_KODE6 = "SPSF";
    public final static String DISKRESJONSKODE_KODE7 = "SPFO";


    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private static final Logger LOG = LoggerFactory.getLogger(TilgangsKontrollService.class);


    @Autowired
    TilgangsKontrollService(EgenAnsattService egenAnsattService) {
        this.egenAnsattService = egenAnsattService;
    }

    @Cacheable(cacheNames = TILGANGTILBRUKER, key = "#saksbehandlerId.concat(#personFnr)", condition = "#personFnr != null && #saksbehandlerId != null")
    public Tilgang sjekkTilgang(String personFnr, String saksbehandlerId, Personinfo personInfo) {

        String diskresjonskode = personInfo.getDiskresjonskode();

        if (DISKRESJONSKODE_KODE6.equals(diskresjonskode) && !harTilgangTilKode6(saksbehandlerId)) {
            secureLogger.info(saksbehandlerId + " har ikke tilgang til " + personFnr);
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE6.name());
        } else if (DISKRESJONSKODE_KODE7.equals(diskresjonskode) && !harTilgangTilKode7(saksbehandlerId)) {
            secureLogger.info(saksbehandlerId + " har ikke tilgang til " + personFnr);
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE7.name());
        }

        if (egenAnsattService.erEgenAnsatt(personFnr) && !harTilgangTilEgenAnsatt(saksbehandlerId)) {
            secureLogger.info(saksbehandlerId + " har ikke tilgang til egen ansatt " + personFnr);
            return new Tilgang().withHarTilgang(false).withBegrunnelse(EGEN_ANSATT.name());
        }

        return new Tilgang().withHarTilgang(true);
    }

    private boolean harTilgangTilKode7(String saksbehandlerId) {
        return false;
        //TODO hent roller fra token eller hent fra graph api til azuread
        //return ldapService.harTilgang(saksbehandlerId, KODE7.rolle);
    }

    private boolean harTilgangTilKode6(String saksbehandlerId) {
        return false;
        //TODO hent roller fra token eller hent fra graph api til azuread
        //return ldapService.harTilgang(saksbehandlerId, KODE6.rolle);
    }

    private boolean harTilgangTilEgenAnsatt(String saksbehandlerId) {
        return false;
        //TODO hent roller fra token eller hent fra graph api til azuread
        //return ldapService.harTilgang(saksbehandlerId, EGEN_ANSATT.rolle);
    }

}
