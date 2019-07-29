package no.nav.familie.ks.oppslag.aktør;

import no.nav.familie.ks.oppslag.aktør.domene.Aktør;
import no.nav.familie.ks.oppslag.aktør.internal.AktørResponse;
import no.nav.familie.ks.oppslag.aktør.internal.AktørregisterClient;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AktørService {

    private AktørregisterClient aktørregisterClient;
    private final CacheManager aktørCacheManager;

    @Autowired
    public AktørService(AktørregisterClient aktørregisterClient, CacheManager aktørCacheManager) {
        this.aktørCacheManager = aktørCacheManager;
        this.aktørregisterClient = aktørregisterClient;
    }

    public String getAktørId(String personIdent) {
        return Optional.ofNullable(aktørCache().get(personIdent)).orElseGet(() -> {
            String aktørId = hentAktørIdFraRegister(personIdent);
            aktørCache().put(personIdent, aktørId);
            return aktørId;
        });
    }

    private String hentAktørIdFraRegister(String personIdent) {
        AktørResponse response = aktørregisterClient.hentAktørId(personIdent);
        Aktør aktørResponse = response.get(personIdent);
        if (aktørResponse.getFeilmelding() == null) {
            return aktørResponse.getIdenter().get(0).getIdent();
        } else {
            throw new RuntimeException(String.format("Feil ved kall mot Aktørregisteret. Feilmelding: %s",
                    aktørResponse.getFeilmelding())
            );
        }
    }

    private Cache<String, String> aktørCache() {
        return aktørCacheManager.getCache("aktørIdCache", String.class, String.class);
    }
}
