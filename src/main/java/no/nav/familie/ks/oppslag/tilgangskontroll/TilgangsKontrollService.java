package no.nav.familie.ks.oppslag.tilgangskontroll;

import no.nav.familie.ks.oppslag.azure.AzureGraphService;
import no.nav.familie.ks.oppslag.azure.domene.Saksbehandler;
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

    private final AzureGraphService azureGraphService;
    private final EgenAnsattService egenAnsattService;

    private final static String ENHET = "ENHET";
    public final static String DISKRESJONSKODE_KODE6 = "SPSF";
    public final static String DISKRESJONSKODE_KODE7 = "SPFO";


    private static final Logger secureLogger = LoggerFactory.getLogger("secureLogger");
    private static final Logger LOG = LoggerFactory.getLogger(TilgangsKontrollService.class);


    @Autowired
    TilgangsKontrollService(
            AzureGraphService azureGraphService,
            EgenAnsattService egenAnsattService) {
        this.azureGraphService = azureGraphService;
        this.egenAnsattService = egenAnsattService;
    }

    @Cacheable(cacheNames = TILGANGTILBRUKER, key = "#saksbehandler.onPremisesSamAccountName.concat(#personFnr)", condition = "#personFnr != null && #saksbehandler.onPremisesSamAccountName != null")
    public Tilgang sjekkTilgang(String personFnr, Saksbehandler saksbehandler, Personinfo personInfo) {
        String NAVident = saksbehandler.getOnPremisesSamAccountName();
        String diskresjonskode = personInfo.getDiskresjonskode();

        if (DISKRESJONSKODE_KODE6.equals(diskresjonskode) && !harTilgangTilKode6(NAVident)) {
            secureLogger.info(NAVident + " har ikke tilgang til " + personFnr);
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE6.name());
        } else if (DISKRESJONSKODE_KODE7.equals(diskresjonskode) && !harTilgangTilKode7(NAVident)) {
            secureLogger.info(NAVident + " har ikke tilgang til " + personFnr);
            return new Tilgang().withHarTilgang(false).withBegrunnelse(KODE7.name());
        }

        if (egenAnsattService.erEgenAnsatt(personFnr) && !harTilgangTilEgenAnsatt(NAVident)) {
            secureLogger.info(NAVident + " har ikke tilgang til egen ansatt " + personFnr);
            return new Tilgang().withHarTilgang(false).withBegrunnelse(EGEN_ANSATT.name());
        }

        return new Tilgang().withHarTilgang(true);
    }

    private boolean harTilgangTilKode7(String NAVident) {
        return false;
        //TODO hent roller fra token eller hent fra graph api til azuread
        //return ldapService.harTilgang(NAVident, KODE7.rolle);
    }

    private boolean harTilgangTilKode6(String NAVident) {
        return false;
        //TODO hent roller fra token eller hent fra graph api til azuread
        //return ldapService.harTilgang(NAVident, KODE6.rolle);
    }

    private boolean harTilgangTilEgenAnsatt(String NAVident) {
        return false;
        //TODO hent roller fra token eller hent fra graph api til azuread
        //return ldapService.harTilgang(NAVident, EGEN_ANSATT.rolle);
    }

}
